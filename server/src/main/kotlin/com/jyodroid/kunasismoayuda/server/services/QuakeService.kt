package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.routes.dto.QuakeResponse
import com.jyodroid.kunasismoayuda.server.upstream.BBox
import com.jyodroid.kunasismoayuda.server.upstream.CountryBBoxes
import com.jyodroid.kunasismoayuda.server.upstream.QuakeSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

/**
 * Aggregates earthquake data: tries the primary source (SGC) first, falls back to USGS, and
 * caches the normalized result for a short window so bursts of clients don't hammer upstream.
 */
class QuakeService(
    private val primary: QuakeSource,
    private val fallback: QuakeSource,
    private val cacheTtlMillis: Long = 60_000,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val logger = LoggerFactory.getLogger(QuakeService::class.java)
    private val mutex = Mutex()

    private data class CacheEntry(val key: String, val at: Long, val data: List<QuakeResponse>)
    private var cache: CacheEntry? = null

    suspend fun recentQuakes(minMagnitude: Double, country: String = "CO"): List<QuakeResponse> {
        val code = country.uppercase()
        val bbox = CountryBBoxes.of(code)
        val key = "min=$minMagnitude;country=$code"
        cache?.let { if (it.key == key && now() - it.at < cacheTtlMillis) return it.data }

        return mutex.withLock {
            cache?.let { if (it.key == key && now() - it.at < cacheTtlMillis) return it.data }

            val quakes = fetchFromSources(minMagnitude, code, bbox).sortedByDescending { it.time }
            cache = CacheEntry(key, now(), quakes)
            quakes
        }
    }

    private suspend fun fetchFromSources(
        minMagnitude: Double,
        country: String,
        bbox: BBox,
    ): List<QuakeResponse> {
        // SGC is Colombia-only; skip it for other countries and go straight to USGS.
        if (country == "CO") {
            runCatching { primary.recentQuakes(minMagnitude, bbox) }
                .onSuccess { if (it.isNotEmpty()) return it }
                .onFailure { logger.warn("Primary source ${primary.name} failed; falling back.", it) }
        }

        return runCatching { fallback.recentQuakes(minMagnitude, bbox) }
            .onFailure { logger.error("Fallback source ${fallback.name} failed.", it) }
            .getOrDefault(emptyList())
    }
}
