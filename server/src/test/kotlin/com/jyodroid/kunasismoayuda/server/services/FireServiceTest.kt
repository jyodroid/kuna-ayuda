package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.routes.dto.FireResponse
import com.jyodroid.kunasismoayuda.server.upstream.BBox
import com.jyodroid.kunasismoayuda.server.upstream.FireSource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FireServiceTest {

    // Distinct lat per fire (>0.1° apart) so hotspot clustering keeps them separate; overridable.
    private var nextLat = 0.0
    private fun fire(id: String, frp: Double?, time: Long, lat: Double = nextLat.also { nextLat += 1.0 }, lon: Double = 0.0) =
        FireResponse(id, time, lat, lon, null, frp, null, null, "TEST", null)

    private fun source(sourceName: String, fires: List<FireResponse>) = object : FireSource {
        override val name = sourceName
        override suspend fun recentFires(bbox: BBox) = fires
    }

    private val emptySource = source("empty", emptyList())

    @Test
    fun ranks_by_frp_descending_and_caps_to_max() = runBlocking {
        val primary = source("primary", listOf(fire("a", 10.0, 1), fire("b", 50.0, 2), fire("c", 30.0, 3)))
        val service = FireService(primary, emptySource, maxFires = 2)
        assertEquals(listOf("b", "c"), service.recentFires("CO").map { it.id })
    }

    @Test
    fun falls_back_when_primary_returns_empty() = runBlocking {
        val fallback = source("fallback", listOf(fire("x", null, 1)))
        val service = FireService(emptySource, fallback)
        assertEquals(listOf("x"), service.recentFires("CO").map { it.id })
    }

    @Test
    fun falls_back_when_primary_throws() = runBlocking {
        val throwing = object : FireSource {
            override val name = "boom"
            override suspend fun recentFires(bbox: BBox): List<FireResponse> = throw RuntimeException("down")
        }
        val fallback = source("fallback", listOf(fire("x", 5.0, 1)))
        val service = FireService(throwing, fallback)
        assertEquals(listOf("x"), service.recentFires("CO").map { it.id })
    }

    @Test
    fun clusters_adjacent_hotspots_keeping_the_most_intense() = runBlocking {
        // Three pixels of ONE fire (same ~0.1° cell) + one distant fire → two entries, and the cluster
        // keeps the highest-FRP pixel (mirrors the "duplicate-looking" list of FIRMS pixels).
        val pixelA = fire("a", 10.0, 1, lat = 11.01, lon = -73.05)
        val pixelB = fire("b", 90.0, 2, lat = 11.02, lon = -73.06) // strongest in the cluster
        val pixelC = fire("c", 30.0, 3, lat = 11.03, lon = -73.07)
        val distant = fire("far", 50.0, 4, lat = 3.05, lon = -75.05)
        val service = FireService(source("p", listOf(pixelA, pixelB, pixelC, distant)), emptySource)

        val result = service.recentFires("CO").map { it.id }
        assertEquals(listOf("b", "far"), result) // cluster collapses to "b"; ranked by FRP (90 > 50)
    }
}
