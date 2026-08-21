package com.jyodroid.kunasismoayuda.core.domain.model

import com.jyodroid.kunasismoayuda.core.domain.util.Geo

/**
 * How close an SOS report is to the responding moderator, so nearby ones can be actioned and far ones
 * delegated to another authority. `NO_LOCATION` covers reports with no coordinates (SAFE check-ins and the
 * empty test taps) — they're grouped separately, never dropped.
 */
enum class SosProximity { NEAR, SAME_CITY, FAR, NO_LOCATION }

/** Distance buckets from the moderator, in km. Tune here. */
object SosProximityThresholds {
    const val NEAR_KM = 2.0
    const val SAME_CITY_KM = 25.0
}

/** Great-circle distance from the moderator to this report, or null when either point is unknown. */
fun SosReport.distanceKmFrom(moderatorLat: Double?, moderatorLon: Double?): Double? {
    if (latitude == null || longitude == null || moderatorLat == null || moderatorLon == null) return null
    return Geo.distanceKm(moderatorLat, moderatorLon, latitude, longitude)
}

/** Bucket this report by distance from the moderator (NO_LOCATION when coordinates are missing). */
fun SosReport.proximityFrom(moderatorLat: Double?, moderatorLon: Double?): SosProximity {
    val d = distanceKmFrom(moderatorLat, moderatorLon) ?: return SosProximity.NO_LOCATION
    return when {
        d <= SosProximityThresholds.NEAR_KM -> SosProximity.NEAR
        d <= SosProximityThresholds.SAME_CITY_KM -> SosProximity.SAME_CITY
        else -> SosProximity.FAR
    }
}
