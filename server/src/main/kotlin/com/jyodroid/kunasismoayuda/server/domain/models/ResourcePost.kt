package com.jyodroid.kunasismoayuda.server.domain.models

import java.time.LocalDateTime

/** A peer request for, or offer of, a resource on the mutual-aid board. */
data class ResourcePost(
    val id: Int,
    val kind: String,          // REQUEST | OFFER
    val resourceType: String,  // WATER | FOOD | MEDICINE | SHELTER | HYGIENE | OTHER
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactEmail: String?,
    val contactName: String?,
    val status: String,        // ACTIVE | PENDING | CLOSED
    val source: String,        // manual | classified
    val rawText: String?,      // original pasted text (classified posts)
    val factCheck: String?,    // moderator-facing Google Fact Check note (classified posts)
    val country: String,       // CO | ID | ES
    val ownerSecret: String?,  // device-local ownership token (#4); null for classified/legacy
    val createdAt: LocalDateTime,
)

data class NewResourcePost(
    val kind: String,
    val resourceType: String,
    val region: String,
    val description: String,
    val contactPhone: String?,
    val contactEmail: String?,
    val contactName: String?,
    val status: String = "ACTIVE",
    val source: String = "manual",
    val rawText: String? = null,
    val factCheck: String? = null,
    val country: String = "CO",
    val ownerSecret: String? = null,
)
