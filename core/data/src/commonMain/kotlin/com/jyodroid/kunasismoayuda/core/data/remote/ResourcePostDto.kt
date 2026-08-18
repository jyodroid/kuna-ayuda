package com.jyodroid.kunasismoayuda.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ResourcePostDto(
    val id: Int,
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String = "",
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val contactName: String? = null,
    val status: String = "ACTIVE",
    val source: String = "manual",
    val rawText: String? = null,
    val factCheck: String? = null,
    val createdAt: String,
    val ownerSecret: String? = null,
)

@Serializable
data class NewResourcePostDto(
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String = "",
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val contactName: String? = null,
    val country: String = "CO",
)

/** Free text pasted from a social post, sent to `/api/board/classify` for Claude to structure. */
@Serializable
data class ClassifyRequestDto(
    val text: String,
    val country: String = "CO",
    val kind: String? = null, // poster's REQUEST/OFFER choice; overrides the model's guess
)

/** What Claude extracted from a paste (`POST /api/board/classify`), shown to the poster to review. */
@Serializable
data class ClassifyPreviewDto(
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String = "",
    val contactPhone: String? = null,
    val contactName: String? = null,
    val factCheck: String? = null,
)

/** Body for `POST /api/board/{id}/resolve` — the device's ownership token. */
@Serializable
data class ResolveRequestDto(
    val secret: String,
)
