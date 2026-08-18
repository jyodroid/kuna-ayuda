package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.routes.dto.FireResponse
import com.jyodroid.kunasismoayuda.server.upstream.BBox
import com.jyodroid.kunasismoayuda.server.upstream.FireSource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FireServiceTest {

    private fun fire(id: String, frp: Double?, time: Long) =
        FireResponse(id, time, 0.0, 0.0, null, frp, null, null, "TEST", null)

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
}
