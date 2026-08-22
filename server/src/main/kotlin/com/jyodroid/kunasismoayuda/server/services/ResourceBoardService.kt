package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.ai.AnthropicClient
import com.jyodroid.kunasismoayuda.server.ai.ClassifiedPost
import com.jyodroid.kunasismoayuda.server.ai.ImageInput
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

/**
 * A confirm-by-cache-handle (image intake) referenced a classify result that's no longer cached — the
 * poster must analyze the screenshot again. Mapped to a clean 410/422 with re-analyze guidance.
 */
class ClassifyExpiredException : RuntimeException("The classify preview expired; analyze again")

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
        val RISK_FLAGS = setOf("ASKS_FOR_MONEY", "UNVERIFIED_CLAIM", "NO_SOURCE")
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_PENDING = "PENDING"
        // Bump when the extraction prompt changes, to invalidate the classify_cache (keyed on this).
        // v5: added vision (screenshot) intake + riskFlags to the extraction.
        private const val PROMPT_VERSION = "v5"
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
        if (isLinkOnly(text)) throw UnclassifiableTextException()
        val hash = contentHash(text)
        val entry = entryFor(hash, country, { anthropic.classify(text, KINDS, TYPES) }, { text })
        return entry.toPreview(kindOverride, cacheRef = hash)
    }

    /**
     * Image intake — classify a SCREENSHOT/photo of a post via vision (Instagram blocks copying text).
     * Reads the text in the image, extracts, and caches by the image bytes so `confirmFromCache` (below)
     * doesn't re-upload or re-pay. Same guards + risk flags as the text path.
     */
    suspend fun classifyImagePreview(imageBytes: ByteArray, mediaType: String, country: String = "CO", kindOverride: String? = null): ClassifyPreviewResponse {
        val hash = imageContentHash(imageBytes)
        val image = ImageInput(Base64.getEncoder().encodeToString(imageBytes), mediaType)
        // Fact-check the extracted description (there's no separate pasted text for an image).
        val entry = entryFor(hash, country, { anthropic.classifyImage(image, KINDS, TYPES) }, { it.description })
        return entry.toPreview(kindOverride, cacheRef = hash)
    }

    /**
     * Step 2 (text) — the poster confirmed the preview; queue it as a PENDING entry for admin moderation.
     * Re-runs the pipeline, which hits the cache from the preview, so no new paid call is made.
     */
    suspend fun classifyAndQueue(text: String, country: String = "CO", kindOverride: String? = null): ResourcePostResponse {
        if (isLinkOnly(text)) throw UnclassifiableTextException()
        val hash = contentHash(text)
        val entry = entryFor(hash, country, { anthropic.classify(text, KINDS, TYPES) }, { text })
        return entry.queue(kindOverride, country, rawText = text.trim())
    }

    /** Step 2 (image) — confirm a previewed classify by its cache handle, without re-uploading the image. */
    fun confirmFromCache(cacheRef: String, country: String = "CO", kindOverride: String? = null): ResourcePostResponse {
        val entry = classifyCache.get(cacheRef) ?: throw ClassifyExpiredException()
        return entry.queue(kindOverride, country, rawText = null)
    }

    /**
     * Shared pipeline: cache-first Claude call (+ usage cap + best-effort fact-check) → empty guard.
     * [classify] runs the paid call on a cache miss; [factCheckSource] yields the text to fact-check
     * (the pasted text, or the extracted description for images). Throws [UnclassifiableTextException]
     * when the extraction is empty.
     */
    private suspend fun entryFor(
        hash: String,
        country: String,
        classify: suspend () -> ClassifiedPost,
        factCheckSource: (ClassifiedPost) -> String,
    ): ClassifyCacheEntry {
        val entry = classifyCache.get(hash) ?: run {
            // Spend cap checked BEFORE the paid call (a hit cap costs nothing); recorded only AFTER success.
            usageLimiter.require(UsageLimiter.FEATURE_ANTHROPIC)
            val classified = classify()
            usageLimiter.record(UsageLimiter.FEATURE_ANTHROPIC)

            val checked = factCheck.isConfigured && usageLimiter.tryConsume(UsageLimiter.FEATURE_FACTCHECK)
            val factCheckNote = if (checked) factCheck.searchSummary(factCheckSource(classified), languageFor(country)) else null

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
                riskFlags = classified.riskFlags.cleanedFlags(),
            ).also { classifyCache.put(hash, it) }
        }
        // Nothing usable (no place AND no description) ⇒ don't queue a blank card. Still cached above.
        if (isEmptyExtraction(entry)) throw UnclassifiableTextException()
        return entry
    }

    private fun ClassifyCacheEntry.toPreview(kindOverride: String?, cacheRef: String) = ClassifyPreviewResponse(
        kind = resolveKind(kindOverride, kind),
        resourceType = resourceType,
        region = region,
        description = description,
        contactPhone = contactPhone,
        contactName = contactName,
        factCheck = factCheck,
        collectionPoints = collectionPoints,
        riskFlags = riskFlags,
        cacheRef = cacheRef,
    )

    private fun ClassifyCacheEntry.queue(kindOverride: String?, country: String, rawText: String?) =
        repository.create(
            NewResourcePost(
                kind = resolveKind(kindOverride, kind),
                resourceType = resourceType,
                region = region,
                description = description,
                contactPhone = contactPhone,
                contactEmail = null,
                contactName = contactName,
                status = STATUS_PENDING,
                source = "classified",
                rawText = rawText,
                factCheck = factCheck,
                country = country,
                collectionPoints = collectionPoints,
                riskFlags = riskFlags,
            ),
        ).toResponse()

    /** Cache key for an image: SHA-256 of the bytes, salted with [PROMPT_VERSION] like the text hash. */
    private fun imageContentHash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update("$PROMPT_VERSION|img|".toByteArray(Charsets.UTF_8))
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
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

    /** Published (ACTIVE) posts across all countries, for moderators to review/remove abusive live posts. */
    fun listActivePosts(): List<ResourcePostResponse> =
        repository.listByStatus(STATUS_ACTIVE).map { it.toResponse() }

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
        riskFlags = riskFlags,
    )

    /** Drop blank/garbage points and trim; keeps only entries that name a place. */
    private fun List<com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint>.cleaned() =
        mapNotNull { p ->
            val name = p.name.trim()
            if (name.isBlank()) return@mapNotNull null
            com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint(name, p.address.trim(), p.hours.trim())
        }.take(12)

    /** Keep only the known risk-flag codes (the model is schema-constrained, but guard anyway), deduped. */
    private fun List<String>.cleanedFlags() =
        map { it.trim().uppercase() }.filter { it in RISK_FLAGS }.distinct().take(5)
}
