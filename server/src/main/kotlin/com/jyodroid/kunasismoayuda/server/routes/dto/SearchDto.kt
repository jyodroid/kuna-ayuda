package com.jyodroid.kunasismoayuda.server.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchReportRequest(
    val subject: String,           // PET | PERSON
    val state: String,             // LOST | FOUND
    val title: String,
    val description: String = "",
    val lastSeen: String,
    val notes: String? = null,
    val contactPhone: String,
    val contactName: String? = null,
    val photoId: Int? = null,
    val country: String = "CO",
)

@Serializable
data class SearchReportResponse(
    val id: Int,
    val subject: String,
    val state: String,
    val title: String,
    val description: String,
    val lastSeen: String,
    val notes: String? = null,
    val contactPhone: String,
    val contactName: String? = null,
    val photoId: Int? = null,
    val country: String,
    val status: String,
    val createdAt: String, // ISO-8601
)

/** Returned by `POST /api/photos` after storing an uploaded image. */
@Serializable
data class PhotoUploadResponse(val id: Int)
