package com.jyodroid.kunasismoayuda.server.routes.dto

import com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint
import kotlinx.serialization.Serializable

@Serializable
data class ResourcePostResponse(
    val id: Int,
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val contactName: String? = null,
    val status: String,
    val source: String = "manual",
    val rawText: String? = null, // original pasted text, for moderators reviewing classified posts
    val factCheck: String? = null, // Google Fact Check note, for moderators reviewing classified posts
    val createdAt: String, // ISO-8601
    // Returned ONLY in the create response, to the poster's own device (never in list/pending).
    val ownerSecret: String? = null,
    val collectionPoints: List<CollectionPoint> = emptyList(), // drop-off/collection points (classified)
    val riskFlags: List<String> = emptyList(), // moderator caution flags (classified) — signal only
)

/** Body for `POST /api/board/{id}/resolve` — the device's ownership token for the post. */
@Serializable
data class ResolveRequest(
    val secret: String,
)

@Serializable
data class ResourcePostRequest(
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String = "",
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val contactName: String? = null,
    val country: String = "CO",
)

/**
 * Free-text pasted from a social post; Claude classifies it into a pending board entry. [kind] is the
 * poster's own REQUEST/OFFER choice — when present it overrides the model's guess (the model still
 * extracts everything else); null lets the model decide.
 */
@Serializable
data class ClassifyRequest(
    val text: String,
    val country: String = "CO",
    val kind: String? = null,
)

/**
 * Confirm-by-handle for the IMAGE intake: after `POST /classify/image` returns a preview with a
 * `cacheRef`, the poster confirms with that handle (no image re-upload; served from the classify cache).
 */
@Serializable
data class ConfirmRefRequest(
    val cacheRef: String,
    val country: String = "CO",
    val kind: String? = null,
)

/**
 * What Claude extracted from a paste, shown back to the poster to review BEFORE it's sent to a
 * moderator. Nothing is persisted yet — the poster confirms via `POST /api/board/classify/confirm`.
 */
@Serializable
data class ClassifyPreviewResponse(
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String,
    val contactPhone: String? = null,
    val contactName: String? = null,
    val factCheck: String? = null,
    val collectionPoints: List<CollectionPoint> = emptyList(),
    val riskFlags: List<String> = emptyList(),
    // Opaque handle to the cached classify result, so the poster can confirm without re-uploading the
    // image (image intake) — see POST /api/board/classify/confirm-ref.
    val cacheRef: String? = null,
)
