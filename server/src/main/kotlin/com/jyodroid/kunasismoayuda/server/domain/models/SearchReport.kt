package com.jyodroid.kunasismoayuda.server.domain.models

import java.time.LocalDateTime

/** A Lost & Found / reunification report (pet or person). Community-submitted, published directly. */
data class SearchReport(
    val id: Int,
    val subject: String,       // PET | PERSON
    val state: String,         // LOST | FOUND
    val title: String,
    val description: String,
    val lastSeen: String,
    val notes: String?,
    val contactPhone: String,
    val contactName: String?,
    val photoId: Int?,
    val country: String,
    val status: String,        // ACTIVE | CLOSED
    val createdAt: LocalDateTime,
)

data class NewSearchReport(
    val subject: String,
    val state: String,
    val title: String,
    val description: String,
    val lastSeen: String,
    val notes: String?,
    val contactPhone: String,
    val contactName: String?,
    val photoId: Int?,
    val country: String = "CO",
    val status: String = "ACTIVE",
)

/** A stored image (bytea). */
class Photo(
    val id: Int,
    val contentType: String,
    val data: ByteArray,
)
