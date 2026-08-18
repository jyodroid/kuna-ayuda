package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A populated Colombian region (department capital / major city) used to estimate which places a
 * quake likely affected and to prioritize the feed toward higher-impact events near people.
 */
data class Region(
    val name: String,
    val department: String,
    val latitude: Double,
    val longitude: Double,
    val population: Int,
)

/**
 * A region a given quake likely affected, with a rough distance and a simple felt-impact score.
 * Higher [impactScore] = more important to surface.
 */
data class AffectedRegion(
    val region: Region,
    val distanceKm: Double,
    val impactScore: Double,
)
