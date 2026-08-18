package com.jyodroid.kunasismoayuda.core.domain.usecase

import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrioritizeByRegionUseCaseTest {

    private val prioritize = PrioritizeByRegionUseCase()

    private fun quake(id: String, mag: Double, lat: Double, lon: Double, time: Long) = Quake(
        id = id,
        timeMillis = time,
        magnitude = mag,
        depthKm = 10.0,
        latitude = lat,
        longitude = lon,
        place = id,
        source = "TEST",
    )

    @Test
    fun larger_quake_near_a_city_outranks_a_bigger_one_in_the_ocean() {
        // Near Bogotá (4.71, -74.07), moderate magnitude.
        val nearCity = quake("near-city", mag = 5.0, lat = 4.7, lon = -74.1, time = 1_000)
        // Far out in the Pacific, larger magnitude but nowhere near people.
        val remote = quake("remote", mag = 6.0, lat = 2.0, lon = -120.0, time = 2_000)

        val result = prioritize(listOf(remote, nearCity))

        assertEquals("near-city", result.first().id, "The quake affecting a populated region should rank first")
    }

    @Test
    fun affected_regions_are_sorted_by_distance_and_within_radius() {
        val nearBogota = quake("q", mag = 5.5, lat = 4.71, lon = -74.07, time = 1)
        val regions = prioritize.affectedRegions(nearBogota, radiusKm = 300.0)

        assertTrue(regions.isNotEmpty(), "Should find affected regions near Bogotá")
        assertEquals("Bogotá", regions.first().region.name)
        // Sorted ascending by distance
        val distances = regions.map { it.distanceKm }
        assertEquals(distances.sorted(), distances)
    }
}
