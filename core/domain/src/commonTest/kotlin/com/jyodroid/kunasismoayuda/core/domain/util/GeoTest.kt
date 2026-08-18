package com.jyodroid.kunasismoayuda.core.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeoTest {

    @Test
    fun same_point_is_zero() {
        assertEquals(0.0, Geo.distanceKm(4.71, -74.07, 4.71, -74.07), 1e-6)
    }

    @Test
    fun one_degree_of_longitude_at_equator_is_about_111km() {
        val km = Geo.distanceKm(0.0, 0.0, 0.0, 1.0)
        assertTrue(km in 111.0..111.4, "expected ~111.19 km, was $km")
    }

    @Test
    fun bogota_to_medellin_is_about_240km() {
        val km = Geo.distanceKm(4.711, -74.072, 6.244, -75.581)
        assertTrue(km in 235.0..250.0, "expected ~240 km, was $km")
    }

    @Test
    fun distance_is_symmetric() {
        val ab = Geo.distanceKm(1.0, 2.0, 3.0, 4.0)
        val ba = Geo.distanceKm(3.0, 4.0, 1.0, 2.0)
        assertEquals(ab, ba, 1e-9)
    }
}
