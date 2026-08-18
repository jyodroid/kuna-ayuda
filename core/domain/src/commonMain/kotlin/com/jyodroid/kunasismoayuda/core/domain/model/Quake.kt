package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A normalized earthquake event. Time is epoch milliseconds (UTC).
 */
data class Quake(
    val id: String,
    val timeMillis: Long,
    val magnitude: Double?,
    val depthKm: Double?,
    val latitude: Double,
    val longitude: Double,
    val place: String,
    val source: String,
    val url: String? = null,
)
