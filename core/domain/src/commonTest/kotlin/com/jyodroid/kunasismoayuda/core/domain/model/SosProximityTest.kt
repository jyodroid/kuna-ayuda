package com.jyodroid.kunasismoayuda.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SosProximityTest {

    private fun report(lat: Double?, lon: Double?) = SosReport(
        id = 1, status = SosStatus.SOS, latitude = lat, longitude = lon,
        region = null, message = null, contactPhone = null, createdAt = "2026-08-20T10:00:00",
    )

    @Test
    fun buckets_by_distance_from_the_moderator() {
        // ~1.1 km north → NEAR (≤2 km)
        assertEquals(SosProximity.NEAR, report(0.01, 0.0).proximityFrom(0.0, 0.0))
        // ~11 km → SAME_CITY (≤25 km)
        assertEquals(SosProximity.SAME_CITY, report(0.10, 0.0).proximityFrom(0.0, 0.0))
        // ~55 km → FAR (>25 km)
        assertEquals(SosProximity.FAR, report(0.50, 0.0).proximityFrom(0.0, 0.0))
    }

    @Test
    fun no_coordinates_or_no_moderator_location_is_no_location() {
        assertEquals(SosProximity.NO_LOCATION, report(null, null).proximityFrom(0.0, 0.0))
        assertEquals(SosProximity.NO_LOCATION, report(0.01, 0.0).proximityFrom(null, null))
    }

    @Test
    fun distanceKmFrom_is_null_without_both_points() {
        assertNull(report(null, null).distanceKmFrom(0.0, 0.0))
        val d = report(0.10, 0.0).distanceKmFrom(0.0, 0.0)
        assertTrue(d != null && d in 10.0..12.0, "expected ~11 km, was $d")
    }
}
