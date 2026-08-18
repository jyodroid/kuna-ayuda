package com.jyodroid.kunasismoayuda.core.data.remote

import kotlinx.serialization.Serializable

/** Wire shape returned by the backend `GET /api/fires` (mirrors the server's FireResponse). */
@Serializable
data class FireDto(
    val id: String,
    val time: Long,
    val latitude: Double,
    val longitude: Double,
    val brightnessK: Double? = null,
    val frpMw: Double? = null,
    val confidence: String? = null,
    val daynight: String? = null,
    val source: String,
    val place: String? = null,
)
