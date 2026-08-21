package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.ai.AnthropicClient
import com.jyodroid.kunasismoayuda.server.domain.models.NewResourcePost
import com.jyodroid.kunasismoayuda.server.domain.models.ResourcePost
import com.jyodroid.kunasismoayuda.server.domain.repositories.ClassifyCacheEntry
import com.jyodroid.kunasismoayuda.server.domain.repositories.ClassifyCacheRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.routes.dto.ClassifyPreviewResponse
import com.jyodroid.kunasismoayuda.server.routes.dto.ResourcePostRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.ResourcePostResponse
import com.jyodroid.kunasismoayuda.server.upstream.FactCheckClient
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Outcome of an owner resolve attempt (device-gated close, #4). */
enum class ResolveOutcome { RESOLVED, NOT_FOUND, FORBIDDEN }

/**
 * The pasted text had nothing to classify — it was just a link (the model can't open URLs), or the
 * model extracted no usable request/offer. We reject it instead of queuing an empty PENDING entry
 * (which would approve into a blank, useless board card). Mapped to a 422 with guidance to paste the
 * post's TEXT rather than a link.
 */
class UnclassifiableTextException : RuntimeException("The pasted text could not be classified")

class ResourceBoardService(
    private val repository: ResourceBoardRepository,
    private val anthropic: AnthropicClient,
    private val usageLimiter: UsageLimiter,
    private val factCheck: FactCheckClient,
    private val classifyCache: ClassifyCacheRepository,
) {

    companion object {
        val KINDS = setOf("REQUEST", "OFFER")
        val TYPES = setOf("WATER", "FOOD", "MEDICINE", "SHELTER", "HYGIENE", "OTHER")
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_PENDING = "PENDING"
        // Bump when the extraction prompt changes, to invalidate the classify_cache (keyed on this).
        private const val PROMPT_VERSION = "v4"
        private val random = SecureRandom()
    }

    /** A ~43-char URL-safe random token that lets the creating device later resolve its own post. */
    private fun newOwnerSecret(): String {
        val bytes = ByteArray(32).also { random.nextBytes(it) }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /** Whether AI classification is available (an API key is configured). */
    val classifyEnabled: Boolean get() = anthropic.isConfigured

    fun list(kind: String?, region: String?, resourceType: String?, country: String = "CO"): List<ResourcePostResponse> =
        repository.listActive(kind, region, resourceType, country).map { it.toResponse() }

    fun create(request: ResourcePostRequest): ResourcePostResponse = repository.create(
        NewResourcePost(
            kind = request.kind.uppercase(),
            resourceType = request.resourceType.uppercase(),
            region = request.region.trim(),
            description = request.description.trim(),
            contactPhone = request.contactPhone?.trim()?.ifBlank { null },
            contactEmail = request.contactEmail?.trim()?.ifBlank { null },
            contactName = request.contactName?.trim()?.ifBlank { null },
            country = request.country,
            ownerSecret = newOwnerSecret(), // device-local ownership token, returned once (below)
        ),
        // Only the create response carries ownerSecret, straight back to the poster's device.
    ).toResponse(includeSecret = true)

    /**
     * Device-gated resolve (#4): close a post if [secret] matches its owner_secret. Constant-time
     * compare. Closing scrubs contact + the secret (see the repo). No auth — ownership is the secret.
     */
    fun resolveByOwner(id: Int, secret: String): ResolveOutcome {
        val post = repository.find(id) ?: return ResolveOutcome.NOT_FOUND
        val stored = post.ownerSecret
        if (stored.isNullOrBlank() || !secretsMatch(stored, secret)) return ResolveOutcome.FORBIDDEN
        repository.close(id)
        return ResolveOutcome.RESOLVED
    }

    private fun secretsMatch(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))

    /**
     * Step 1 — classify the paste and return a **preview** for the poster to review. Nothing is
     * persisted yet: the poster confirms via [classifyAndQueue] before it reaches a moderator. Runs the
     * link/empty guards and the paid call (cached), so the confirm step below is free.
     */
    suspend fun classifyPreview(text: String, country: String = "CO", kindOverride: String? = null): ClassifyPreviewResponse {
        val entry = classifyToEntry(text, country)
        return ClassifyPreviewResponse(
            kind = resolveKind(kindOverride, entry.kind),
            resourceType = entry.resourceType,
            region = entry.region,
            description = entry.description,
            contactPhone = entry.contactPhone,
            contactName = entry.contactName,
            factCheck = entry.factCheck,
            collectionPoints = entry.collectionPoints,
        )
    }

    /**
     * Step 2 — the poster confirmed the preview; queue it as a PENDING entry for admin moderation
     * (anti-fraud — classified content is never public until an admin approves it). Re-runs
     * [classifyToEntry], which hits the cache from the preview, so no new paid call is made.
     */
    suspend fun classifyAndQueue(text: String, country: String = "CO", kindOverride: String? = null): ResourcePostResponse {
        val entry = classifyToEntry(text, country)
        return repository.create(
            NewResourcePost(
                kind = resolveKind(kindOverride, entry.kind),
                resourceType = entry.resourceType,
                region = entry.region,
                description = entry.description,
                contactPhone = entry.contactPhone,
                contactEmail = null,
                contactName = entry.contactName,
                status = STATUS_PENDING,
                source = "classified",
                rawText = text.trim(),
                factCheck = entry.factCheck,
                country = country,
                collectionPoints = entry.collectionPoints,
            ),
        ).toResponse()
    }

    /**
     * Shared classify pipeline for both preview and confirm: link guard → cache-first Claude call
     * (+ usage cap + best-effort fact-check) → empty guard. Throws [UnclassifiableTextException] when
     * there's nothing to classify (a bare link, or an empty extraction).
     */
    private suspend fun classifyToEntry(text: String, country: String): ClassifyCacheEntry {
        // Cheap pre-check BEFORE any paid call: a paste that's just a link (the model can't open URLs)
        // has nothing to classify — reject with guidance instead of spending on an empty extraction.
        if (isLinkOnly(text)) throw UnclassifiableTextException()

        val hash = contentHash(text)

        // Cache-first: the same post (often a viral one, or a preview being confirmed) is served from
        // the memoized result — no Anthropic, no Fact Check, no usage consumed.
        val entry = classifyCache.get(hash) ?: run {
            // Spend cap: stop (429) before spending once the monthly cap is reached. We check BEFORE the
            // paid call (so a hit cap costs nothing) and only record AFTER it succeeds (so a failed call
            // — e.g. a bad key — doesn't burn the budget).
            usageLimiter.require(UsageLimiter.FEATURE_ANTHROPIC)
            val classified = anthropic.classify(text, KINDS, TYPES)
            usageLimiter.record(UsageLimiter.FEATURE_ANTHROPIC)

            // Fact-check as a moderator signal (best-effort): only if a key is configured AND its own
            // monthly cap isn't spent. Failures/no-matches ⇒ null, never blocks classification.
            val checked = factCheck.isConfigured && usageLimiter.tryConsume(UsageLimiter.FEATURE_FACTCHECK)
            val factCheckNote = if (checked) factCheck.searchSummary(text, languageFor(country)) else null

            ClassifyCacheEntry(
                kind = classified.kind.uppercase().takeIf { it in KINDS } ?: "REQUEST",
                resourceType = classified.resourceType.uppercase().takeIf { it in TYPES } ?: "OTHER",
                region = classified.region.trim(),
                description = classified.description.trim(),
                contactPhone = classified.contactPhone.trim().ifBlank { null },
                contactName = classified.contactName.trim().ifBlank { null },
                factCheck = factCheckNote,
                checked = checked,
                collectionPoints = classified.collectionPoints.cleaned(),
            ).also { classifyCache.put(hash, it) }
        }

        // Post-classification guard: if the model extracted nothing usable (no place AND no
        // description — a blank "OTHER"), don't queue it. Approving such an entry would publish a
        // useless empty card. The result is still cached above, so a re-paste won't pay again.
        if (isEmptyExtraction(entry)) throw UnclassifiableTextException()
        return entry
    }

    /** The poster's own REQUEST/OFFER choice wins over the model's guess (when it's a valid kind). */
    private fun resolveKind(override: String?, modelKind: String): String =
        override?.uppercase()?.takeIf { it in KINDS } ?: modelKind

    /** A URL matcher used to tell "just a link" pastes apart from real post text. */
    private val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)

    /** True when the paste is essentially only a link — a URL with no surrounding words to classify. */
    private fun isLinkOnly(text: String): Boolean =
        urlRegex.containsMatchIn(text) && urlRegex.replace(text, " ").none { it.isLetterOrDigit() }

    /** True when the model returned no usable content (no region AND no description). */
    private fun isEmptyExtraction(entry: ClassifyCacheEntry): Boolean =
        entry.region.isBlank() && entry.description.isBlank()

    /** SHA-256 (hex) of the normalized pasted text — the cache key. Normalizing (lowercase + collapsed
     *  whitespace) makes trivially-reformatted reposts of the same content hit the same cache row. The
     *  [PROMPT_VERSION] salt invalidates the cache when the extraction prompt changes, so a prompt fix
     *  isn't shadowed by results memoized under the old prompt. */
    private fun contentHash(text: String): String {
        val normalized = PROMPT_VERSION + "|" + text.trim().lowercase().replace(Regex("\\s+"), " ")
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** Best-effort BCP-47 language for the Fact Check query, from the selected country. */
    private fun languageFor(country: String): String = when (country.uppercase()) {
        "ID" -> "id"
        "IT" -> "it"
        else -> "es" // CO, ES (and default)
    }

    /** Admin: posts awaiting moderation. */
    fun listPending(): List<ResourcePostResponse> =
        repository.listByStatus(STATUS_PENDING).map { it.toResponse() }

    /** The raw domain post by id (for the audit before-snapshot); null if absent. */
    fun find(id: Int): ResourcePost? = repository.find(id)

    /** Admin: publish a pending post. */
    fun approve(id: Int): Boolean = repository.setStatus(id, STATUS_ACTIVE)

    fun close(id: Int): Boolean = repository.close(id)

    // includeSecret is true ONLY for the create response (to the poster's device); list/pending never
    // expose ownerSecret — it's the ownership credential.
    private fun ResourcePost.toResponse(includeSecret: Boolean = false) = ResourcePostResponse(
        id = id,
        kind = kind,
        resourceType = resourceType,
        region = region,
        description = description,
        contactPhone = contactPhone,
        contactEmail = contactEmail,
        contactName = contactName,
        status = status,
        source = source,
        rawText = rawText,
        factCheck = factCheck,
        createdAt = createdAt.toString(),
        ownerSecret = if (includeSecret) ownerSecret else null,
        collectionPoints = collectionPoints,
    )

    /** Drop blank/garbage points and trim; keeps only entries that name a place. */
    private fun List<com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint>.cleaned() =
        mapNotNull { p ->
            val name = p.name.trim()
            if (name.isBlank()) return@mapNotNull null
            com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint(name, p.address.trim(), p.hours.trim())
        }.take(12)
}
