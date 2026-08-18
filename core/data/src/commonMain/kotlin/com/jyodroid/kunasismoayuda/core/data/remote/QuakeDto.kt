package com.jyodroid.kunasismoayuda.core.data.remote

import kotlinx.serialization.Serializable

/** Wire shape returned by the backend `GET /api/quakes` (mirrors the server's QuakeResponse). */
@Serializable
data class QuakeDto(
    val id: String,
    val time: Long,
    val magnitude: Double? = null,
    val depthKm: Double? = null,
    val latitude: Double,
    val longitude: Double,
    val place: String,
    val source: String,
    val url: String? = null,
)
