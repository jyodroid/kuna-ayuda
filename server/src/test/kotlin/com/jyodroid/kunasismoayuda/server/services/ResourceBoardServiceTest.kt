package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.ai.AnthropicClient
import com.jyodroid.kunasismoayuda.server.ai.ClassifiedPost
import com.jyodroid.kunasismoayuda.server.domain.models.NewResourcePost
import com.jyodroid.kunasismoayuda.server.domain.models.ResourcePost
import com.jyodroid.kunasismoayuda.server.domain.repositories.ApiUsageRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ClassifyCacheEntry
import com.jyodroid.kunasismoayuda.server.domain.repositories.ClassifyCacheRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.upstream.FactCheckClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ResourceBoardServiceTest {

    // ---- In-memory fakes ------------------------------------------------------------------------

    /** Minimal in-memory board repo: auto-increment ids, and close() scrubs like the real one. */
    private class FakeBoardRepo : ResourceBoardRepository {
        val posts = mutableListOf<ResourcePost>()
        private var nextId = 1

        override fun listActive(kind: String?, region: String?, resourceType: String?, country: String) =
            posts.filter { it.status == "ACTIVE" }

        override fun listByStatus(status: String) = posts.filter { it.status == status }

        override fun create(post: NewResourcePost): ResourcePost {
            val row = ResourcePost(
                id = nextId++,
                kind = post.kind,
                resourceType = post.resourceType,
                region = post.region,
                description = post.description,
                contactPhone = post.contactPhone,
                contactEmail = post.contactEmail,
                contactName = post.contactName,
                status = post.status,
                source = post.source,
                rawText = post.rawText,
                factCheck = post.factCheck,
                country = post.country,
                ownerSecret = post.ownerSecret,
                collectionPoints = post.collectionPoints,
                riskFlags = post.riskFlags,
                createdAt = LocalDateTime.now(),
            )
            posts.add(row)
            return row
        }

        override fun find(id: Int) = posts.firstOrNull { it.id == id }

        override fun setStatus(id: Int, status: String): Boolean {
            val i = posts.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return false
            posts[i] = posts[i].copy(status = status)
            return true
        }

        override fun close(id: Int): Boolean {
            val i = posts.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return false
            // Mirror the real repo: close scrubs contact + the ownership secret.
            posts[i] = posts[i].copy(
                status = "CLOSED",
                contactPhone = null,
                contactEmail = null,
                contactName = null,
                ownerSecret = null,
            )
            return true
        }

        override fun restore(
            id: Int,
            status: String,
            contactPhone: String?,
            contactEmail: String?,
            contactName: String?,
            ownerSecret: String?,
        ): Boolean {
            val i = posts.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return false
            posts[i] = posts[i].copy(
                status = status,
                contactPhone = contactPhone,
                contactEmail = contactEmail,
                contactName = contactName,
                ownerSecret = ownerSecret,
            )
            return true
        }

        override fun expireOlderThan(cutoff: LocalDateTime) = 0
        override fun deleteOlderThan(cutoff: LocalDateTime) = 0
    }

    private class FakeCache : ClassifyCacheRepository {
        val store = mutableMapOf<String, ClassifyCacheEntry>()
        override fun get(contentHash: String) = store[contentHash]
        override fun put(contentHash: String, entry: ClassifyCacheEntry) { store[contentHash] = entry }
    }

    // No DB in unit tests → the limiter uses its in-memory path and never touches this repo.
    private val unusedUsageRepo = object : ApiUsageRepository {
        override fun tryConsume(feature: String, period: String, limit: Int) = error("db not used")
        override fun increment(feature: String, period: String) = error("db not used")
        override fun countFor(feature: String, period: String): Int = error("db not used")
    }

    private fun limiter(anthropicLimit: Int? = null) = UsageLimiter(
        unusedUsageRepo,
        mapOf(UsageLimiter.FEATURE_ANTHROPIC to anthropicLimit, UsageLimiter.FEATURE_FACTCHECK to 0),
    )

    private fun classified(
        kind: String = "REQUEST",
        resourceType: String = "WATER",
        region: String = "Quibdó",
        description: String = "Se necesita agua potable en el barrio central.",
    ) = ClassifiedPost(kind, resourceType, region, description, "", "")

    /** Builds a service with a mocked Anthropic/FactCheck and real in-memory fakes. */
    private fun service(
        repo: FakeBoardRepo = FakeBoardRepo(),
        cache: FakeCache = FakeCache(),
        usage: UsageLimiter = limiter(),
        anthropic: AnthropicClient = mockk {
            every { isConfigured } returns true
            coEvery { classify(any(), any(), any()) } returns classified()
        },
        factCheck: FactCheckClient = mockk { every { isConfigured } returns false },
    ) = Triple(ResourceBoardService(repo, anthropic, usage, factCheck, cache), repo, anthropic)

    // ---- classify guards ------------------------------------------------------------------------

    @Test
    fun link_only_paste_is_rejected_before_any_paid_call() = runBlocking {
        val (svc, _, anthropic) = service()
        assertThrows<UnclassifiableTextException> {
            runBlocking { svc.classifyPreview("https://instagram.com/p/abc123", "CO") }
        }
        coVerify(exactly = 0) { anthropic.classify(any(), any(), any()) }
    }

    @Test
    fun empty_extraction_is_rejected() {
        val anthropic = mockk<AnthropicClient> {
            every { isConfigured } returns true
            coEvery { classify(any(), any(), any()) } returns classified(region = "", description = "")
        }
        val (svc, _, _) = service(anthropic = anthropic)
        assertThrows<UnclassifiableTextException> {
            runBlocking { svc.classifyPreview("algo sin contenido util", "CO") }
        }
    }

    @Test
    fun preview_returns_the_models_extraction() = runBlocking {
        val (svc, _, _) = service()
        val preview = svc.classifyPreview("Se necesita agua en Quibdó", "CO")
        assertEquals("REQUEST", preview.kind)
        assertEquals("WATER", preview.resourceType)
        assertEquals("Quibdó", preview.region)
    }

    // ---- kind override --------------------------------------------------------------------------

    @Test
    fun poster_kind_override_wins_over_the_model_guess() = runBlocking {
        val (svc, _, _) = service() // model says REQUEST
        val preview = svc.classifyPreview("texto", "CO", kindOverride = "offer")
        assertEquals("OFFER", preview.kind)
    }

    @Test
    fun invalid_kind_override_falls_back_to_the_model() = runBlocking {
        val (svc, _, _) = service() // model says REQUEST
        val preview = svc.classifyPreview("texto", "CO", kindOverride = "NONSENSE")
        assertEquals("REQUEST", preview.kind)
    }

    // ---- cache + hash normalization -------------------------------------------------------------

    @Test
    fun repeat_paste_is_served_from_cache_without_a_second_paid_call() = runBlocking {
        val (svc, _, anthropic) = service()
        svc.classifyPreview("Se necesita agua en Quibdó", "CO")
        svc.classifyPreview("Se necesita agua en Quibdó", "CO")
        coVerify(exactly = 1) { anthropic.classify(any(), any(), any()) }
    }

    @Test
    fun reformatted_repost_hits_the_same_cache_row() = runBlocking {
        val (svc, _, anthropic) = service()
        svc.classifyPreview("Se necesita AGUA en Quibdó", "CO")
        // Same text, different case + collapsed/extra whitespace → normalizes to the same hash.
        svc.classifyPreview("  se necesita   agua en quibdó  ", "CO")
        coVerify(exactly = 1) { anthropic.classify(any(), any(), any()) }
    }

    @Test
    fun confirm_reuses_the_preview_cache_so_it_makes_no_new_paid_call() = runBlocking {
        val (svc, repo, anthropic) = service()
        val text = "Se necesita agua en Quibdó"
        svc.classifyPreview(text, "CO")
        svc.classifyAndQueue(text, "CO")
        coVerify(exactly = 1) { anthropic.classify(any(), any(), any()) }
        assertEquals(1, repo.posts.size)
    }

    // ---- queue ----------------------------------------------------------------------------------

    @Test
    fun confirm_queues_a_pending_classified_post_with_raw_text() = runBlocking {
        val (svc, repo, _) = service()
        svc.classifyAndQueue("Se necesita agua en Quibdó", "CO", kindOverride = "OFFER")
        val post = repo.posts.single()
        assertEquals("PENDING", post.status)
        assertEquals("classified", post.source)
        assertEquals("OFFER", post.kind) // override applied
        assertEquals("Se necesita agua en Quibdó", post.rawText)
    }

    // ---- edited confirm -------------------------------------------------------------------------

    private fun cacheEntry() = ClassifyCacheEntry(
        kind = "REQUEST",
        resourceType = "WATER",
        region = "Quibdó",
        description = "Se necesita agua.",
        contactPhone = "3001111111 3002222222", // the bad two-number join the poster will fix
        contactName = "Cruz Roja",
        factCheck = "Nota de verificación",
        checked = true,
        collectionPoints = listOf(
            com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint("Parroquia", "Calle 5", "8-12"),
        ),
        riskFlags = listOf("ASKS_FOR_MONEY"),
    )

    @Test
    fun confirm_edited_persists_edits_and_keeps_cached_signals() {
        val repo = FakeBoardRepo()
        val cache = FakeCache().apply { put("ref-1", cacheEntry()) }
        val (svc, _, _) = service(repo = repo, cache = cache)

        svc.confirmEdited(
            com.jyodroid.kunasismoayuda.server.routes.dto.ConfirmClassifyRequest(
                cacheRef = "ref-1",
                kind = "OFFER",              // poster corrected the kind
                resourceType = "FOOD",       // and the type
                region = "Cali",             // and the region
                description = "Entregamos comida.",
                contactPhone = "3001111111", // fixed to a single number
                contactName = "Voluntarios",
                rawText = "texto original",
                country = "CO",
            ),
        )

        val post = repo.posts.single()
        // Edited content is what the poster sent.
        assertEquals("PENDING", post.status)
        assertEquals("classified", post.source)
        assertEquals("OFFER", post.kind)
        assertEquals("FOOD", post.resourceType)
        assertEquals("Cali", post.region)
        assertEquals("3001111111", post.contactPhone)
        assertEquals("Voluntarios", post.contactName)
        assertEquals("texto original", post.rawText)
        // Moderation signals + collection points are kept from the cache (not the client).
        assertEquals("Nota de verificación", post.factCheck)
        assertEquals(listOf("ASKS_FOR_MONEY"), post.riskFlags)
        assertEquals(1, post.collectionPoints.size)
        assertEquals("Parroquia", post.collectionPoints.single().name)
    }

    @Test
    fun confirm_edited_with_an_expired_cache_ref_is_rejected() {
        val (svc, _, _) = service()
        assertThrows<ClassifyExpiredException> {
            svc.confirmEdited(
                com.jyodroid.kunasismoayuda.server.routes.dto.ConfirmClassifyRequest(
                    cacheRef = "missing", kind = "REQUEST", resourceType = "WATER",
                    region = "Cali", description = "agua",
                ),
            )
        }
    }

    // ---- usage cap ------------------------------------------------------------------------------

    @Test
    fun cap_reached_blocks_before_the_paid_call() = runBlocking {
        val (svc, _, anthropic) = service(usage = limiter(anthropicLimit = 0))
        assertThrows<UsageLimitReachedException> {
            runBlocking { svc.classifyPreview("texto nuevo", "CO") }
        }
        coVerify(exactly = 0) { anthropic.classify(any(), any(), any()) }
    }

    @Test
    fun usage_is_recorded_so_the_next_distinct_paste_hits_the_cap() {
        val (svc, _, _) = service(usage = limiter(anthropicLimit = 1))
        runBlocking { svc.classifyPreview("primer texto en Quibdó", "CO") } // consumes the single allowance
        assertThrows<UsageLimitReachedException> {
            runBlocking { svc.classifyPreview("segundo texto distinto en Cali", "CO") }
        }
    }

    // ---- device-gated resolve (#4) --------------------------------------------------------------

    @Test
    fun create_returns_the_owner_secret_once() {
        val (svc, _, _) = service()
        val created = svc.create(
            com.jyodroid.kunasismoayuda.server.routes.dto.ResourcePostRequest(
                kind = "REQUEST", resourceType = "WATER", region = "Cali",
                description = "agua", country = "CO",
            ),
        )
        assertNotNull(created.ownerSecret)
    }

    @Test
    fun resolve_with_the_matching_secret_closes_and_scrubs_the_post() {
        val repo = FakeBoardRepo()
        val (svc, _, _) = service(repo = repo)
        val created = svc.create(
            com.jyodroid.kunasismoayuda.server.routes.dto.ResourcePostRequest(
                kind = "OFFER", resourceType = "FOOD", region = "Cali",
                description = "comida", contactPhone = "3001234567", country = "CO",
            ),
        )
        val outcome = svc.resolveByOwner(created.id, created.ownerSecret!!)
        assertEquals(ResolveOutcome.RESOLVED, outcome)
        val closed = repo.find(created.id)!!
        assertEquals("CLOSED", closed.status)
        assertNull(closed.contactPhone)   // scrubbed
        assertNull(closed.ownerSecret)    // scrubbed
    }

    @Test
    fun resolve_with_a_wrong_secret_is_forbidden_and_leaves_the_post_open() {
        val repo = FakeBoardRepo()
        val (svc, _, _) = service(repo = repo)
        val created = svc.create(
            com.jyodroid.kunasismoayuda.server.routes.dto.ResourcePostRequest(
                kind = "REQUEST", resourceType = "WATER", region = "Cali",
                description = "agua", country = "CO",
            ),
        )
        assertEquals(ResolveOutcome.FORBIDDEN, svc.resolveByOwner(created.id, "not-the-secret"))
        assertEquals("ACTIVE", repo.find(created.id)!!.status)
    }

    @Test
    fun resolve_of_an_unknown_id_is_not_found() {
        val (svc, _, _) = service()
        assertEquals(ResolveOutcome.NOT_FOUND, svc.resolveByOwner(999, "whatever"))
    }
}
