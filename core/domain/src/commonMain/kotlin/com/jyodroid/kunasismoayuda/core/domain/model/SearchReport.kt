package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A lost-or-found report on the reunification board — a **pet** or a **person** that's missing or has
 * been found. Community-submitted and published directly (no moderation), like the aid board. A
 * [photoId] references an uploaded image served by the backend (`GET /api/photos/{id}`).
 */
data class SearchReport(
    val id: Int,
    val subject: SearchSubject,
    val status: SearchStatus,
    val title: String,          // pet name / person's name or alias
    val description: String,    // species, breed, distinguishing features…
    val lastSeen: String,       // region / municipality
    val notes: String?,         // free-text extra detail (last-seen place, circumstances)
    val contactPhone: String,
    val contactName: String?,
    val photoId: Int?,
    val country: String,
    val createdAt: String,
)

/** Fields a user provides to publish a report; the photo is uploaded separately and referenced by id. */
data class NewSearchReport(
    val subject: SearchSubject,
    val status: SearchStatus,
    val title: String,
    val description: String,
    val lastSeen: String,
    val notes: String?,
    val contactPhone: String,
    val contactName: String?,
    val country: String = "CO",
)

enum class SearchSubject {
    PET, PERSON;

    companion object {
        fun fromRaw(raw: String): SearchSubject =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: PET
    }
}

enum class SearchStatus {
    LOST, FOUND;

    companion object {
        fun fromRaw(raw: String): SearchStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: LOST
    }
}
