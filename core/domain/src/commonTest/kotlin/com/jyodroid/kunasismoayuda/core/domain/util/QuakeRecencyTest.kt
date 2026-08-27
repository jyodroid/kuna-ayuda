package com.jyodroid.kunasismoayuda.core.domain.util

import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuakeRecencyTest {

    private val now = 1_000_000_000_000L
    private val hour = 3_600_000L
    private val window = 48 * hour

    private fun quake(id: String, ageHours: Long, mag: Double = 4.0) = Quake(
        id = id,
        timeMillis = now - ageHours * hour,
        magnitude = mag,
        depthKm = 10.0,
        latitude = 4.0,
        longitude = -74.0,
        place = "Test",
        source = "test",
    )

    @Test
    fun returns_the_freshest_quake_within_the_window() {
        val quakes = listOf(quake("old", ageHours = 300), quake("fresh", ageHours = 3))
        assertEquals("fresh", mostRecentQuake(quakes, now, window, excludeId = null)?.id)
    }

    @Test
    fun excludes_the_headline_so_it_never_duplicates() {
        val quakes = listOf(quake("fresh", ageHours = 3))
        assertNull(mostRecentQuake(quakes, now, window, excludeId = "fresh"))
    }

    @Test
    fun returns_null_when_the_freshest_is_older_than_the_window() {
        val quakes = listOf(quake("stale", ageHours = 72))
        assertNull(mostRecentQuake(quakes, now, window, excludeId = null))
    }

    @Test
    fun empty_feed_is_null() {
        assertNull(mostRecentQuake(emptyList(), now, window, excludeId = null))
    }

    @Test
    fun is_today_within_24h_true_beyond_false() {
        assertTrue(isToday(now, now - 23 * hour))
        assertTrue(isToday(now, now - 24 * hour))
        assertFalse(isToday(now, now - 25 * hour))
        // Future / clock-skewed timestamp counts as today.
        assertTrue(isToday(now, now + hour))
    }
}
