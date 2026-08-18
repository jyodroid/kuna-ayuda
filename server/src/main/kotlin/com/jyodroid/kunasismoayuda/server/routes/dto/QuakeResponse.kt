package com.jyodroid.kunasismoayuda.server.routes.dto

import kotlinx.serialization.Serializable

/**
 * Normalized earthquake shape the KMP client consumes. Independent of whichever upstream
 * (SGC or USGS) produced it — the client agrees with the server on this contract by JSON shape.
 */
@Serializable
data class QuakeResponse(
    val id: String,
    val time: Long,            // epoch millis (UTC)
    val magnitude: Double?,
    val depthKm: Double?,
    val latitude: Double,
    val longitude: Double,
    val place: String,
    val source: String,        // "SGC" or "USGS"
    val url: String? = null,
)
