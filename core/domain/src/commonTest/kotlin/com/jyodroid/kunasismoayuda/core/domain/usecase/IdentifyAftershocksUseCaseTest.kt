package com.jyodroid.kunasismoayuda.core.domain.usecase

import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentifyAftershocksUseCaseTest {

    private val useCase = IdentifyAftershocksUseCase() // 120 km, ±3 days
    private val day = 24L * 60 * 60 * 1000

    private fun quake(id: String, lat: Double, lon: Double, time: Long) =
        Quake(id, time, 4.0, 10.0, lat, lon, id, "TEST")

    private val main = quake("main", lat = 4.7, lon = -74.1, time = 10 * day)

    @Test
    fun includes_events_within_radius_and_window_and_excludes_main() {
        val near = quake("near", lat = 4.8, lon = -74.1, time = 10 * day + day) // ~11 km, +1 day
        val result = useCase(main, listOf(main, near))
        assertEquals(listOf("near"), result.map { it.id })
        assertFalse(result.any { it.id == "main" })
    }

    @Test
    fun excludes_events_too_far_away() {
        val faraway = quake("far", lat = 10.0, lon = -75.0, time = 10 * day) // >120 km
        assertTrue(useCase(main, listOf(main, faraway)).isEmpty())
    }

    @Test
    fun excludes_events_outside_the_time_window() {
        val old = quake("old", lat = 4.71, lon = -74.1, time = 10 * day - 5 * day) // 5 days before
        assertTrue(useCase(main, listOf(main, old)).isEmpty())
    }

    @Test
    fun returns_most_recent_first() {
        val a = quake("a", lat = 4.71, lon = -74.1, time = 10 * day + 1)
        val b = quake("b", lat = 4.71, lon = -74.1, time = 10 * day + 2 * day)
        val result = useCase(main, listOf(a, b, main))
        assertEquals(listOf("b", "a"), result.map { it.id })
    }
}
