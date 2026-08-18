package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.repositories.ApiUsageRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UsageLimiterTest {

    // No DB in unit tests → DatabaseFactory.initialized is false → the in-memory path is used and this
    // repo is never called (it throws if it is, catching accidental DB coupling).
    private val unusedRepo = object : ApiUsageRepository {
        override fun tryConsume(feature: String, period: String, limit: Int) = error("db not used")
        override fun increment(feature: String, period: String) = error("db not used")
        override fun countFor(feature: String, period: String): Int = error("db not used")
    }

    private fun limiter(limit: Int?) = UsageLimiter(unusedRepo, mapOf("f" to limit))

    @Test
    fun limits_from_env_parse_rules() {
        val env = mapOf(
            "ANTHROPIC_MONTHLY_LIMIT" to "5",
            "FACTCHECK_MONTHLY_LIMIT" to "   ", // blank → unlimited
        )
        val limits = UsageLimiter.limitsFromEnv { env[it] }
        assertEquals(5, limits[UsageLimiter.FEATURE_ANTHROPIC])
        assertNull(limits[UsageLimiter.FEATURE_FACTCHECK])
    }

    @Test
    fun unlimited_allows_everything() {
        val l = limiter(null)
        assertTrue(l.check("f"))
        assertTrue(l.tryConsume("f"))
        l.require("f") // does not throw
    }

    @Test
    fun disabled_zero_blocks_and_throws() {
        val l = limiter(0)
        assertFalse(l.check("f"))
        assertFalse(l.tryConsume("f"))
        assertThrows<UsageLimitReachedException> { l.require("f") }
    }

    @Test
    fun finite_limit_counts_then_stops() {
        val l = limiter(2)
        assertTrue(l.check("f")); l.record("f")
        assertTrue(l.check("f")); l.record("f")
        assertFalse(l.check("f"))
        assertThrows<UsageLimitReachedException> { l.require("f") }
    }

    @Test
    fun tryConsume_stops_after_the_cap() {
        val l = limiter(2)
        assertTrue(l.tryConsume("f"))
        assertTrue(l.tryConsume("f"))
        assertFalse(l.tryConsume("f"))
    }
}
