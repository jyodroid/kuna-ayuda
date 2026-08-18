package com.jyodroid.kunasismoayuda.core.domain.model

/** A peer request for, or offer of, a resource on the mutual-aid board. Community-submitted. */
data class ResourcePost(
    val id: Int,
    val kind: PostKind,
    val resourceType: ResourceType,
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactEmail: String?,
    val contactName: String?,
    val createdAt: String,
    val source: String = "manual", // manual | classified — how the entry originated
    val rawText: String? = null,   // original pasted text (classified posts), shown to moderators
    val factCheck: String? = null, // Google Fact Check note (classified posts), shown to moderators
    val ownerSecret: String? = null, // device ownership token — only present on the create response (#4)
)

/** Fields a user provides to publish a post. */
data class NewResourcePost(
    val kind: PostKind,
    val resourceType: ResourceType,
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactEmail: String?,
    val contactName: String?,
    val country: String = "CO",
)

enum class PostKind {
    REQUEST, OFFER;

    companion object {
        fun fromRaw(raw: String): PostKind =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: REQUEST
    }
}

enum class ResourceType {
    WATER, FOOD, MEDICINE, SHELTER, HYGIENE, OTHER;

    companion object {
        fun fromRaw(raw: String): ResourceType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OTHER
    }
}
