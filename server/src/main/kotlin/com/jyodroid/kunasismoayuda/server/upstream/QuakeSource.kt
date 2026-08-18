package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.routes.dto.QuakeResponse

/** A source of recent earthquakes within a bounding box. */
interface QuakeSource {
    val name: String
    suspend fun recentQuakes(minMagnitude: Double, bbox: BBox): List<QuakeResponse>
}

/** A geographic bounding box used to filter global feeds to a country/region. */
data class BBox(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
) {
    fun contains(lat: Double, lon: Double): Boolean =
        lat in minLat..maxLat && lon in minLon..maxLon
}

/**
 * Per-country bounding boxes for the supported countries (must mirror the client's `Country` enum).
 * The client sends `?country=CO|ID|ES`; unknown codes fall back to Colombia.
 */
object CountryBBoxes {
    val COLOMBIA = BBox(minLat = -4.5, maxLat = 13.5, minLon = -79.5, maxLon = -66.0)
    val INDONESIA = BBox(minLat = -11.0, maxLat = 6.0, minLon = 95.0, maxLon = 141.0)
    val SPAIN = BBox(minLat = 35.0, maxLat = 44.0, minLon = -9.5, maxLon = 4.5)
    val ITALY = BBox(minLat = 35.4, maxLat = 47.1, minLon = 6.6, maxLon = 18.6)

    fun of(code: String?): BBox = when (code?.uppercase()) {
        "ID" -> INDONESIA
        "ES" -> SPAIN
        "IT" -> ITALY
        else -> COLOMBIA
    }
}

/** Colombia's approximate bounding box, still used by the GDACS/ReliefWeb ingestion (Colombia-only). */
object ColombiaBBox {
    const val MIN_LAT = -4.5
    const val MAX_LAT = 13.5
    const val MIN_LON = -79.5
    const val MAX_LON = -66.0

    fun contains(lat: Double, lon: Double): Boolean =
        lat in MIN_LAT..MAX_LAT && lon in MIN_LON..MAX_LON
}
