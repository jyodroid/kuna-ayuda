package com.jyodroid.kunasismoayuda.server.routes.dto

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
)
