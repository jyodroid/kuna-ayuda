package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.routes.dto.FireResponse
import com.jyodroid.kunasismoayuda.server.upstream.BBox
import com.jyodroid.kunasismoayuda.server.upstream.CountryBBoxes
import com.jyodroid.kunasismoayuda.server.upstream.FireSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

/**
 * Aggregates active-wildfire data — the wildfire analog of [QuakeService]. Tries the primary source
 * (NASA FIRMS points) first; when it's disabled (no `FIRMS_MAP_KEY`) or returns nothing, falls back to
 * keyless GDACS wildfire events. Normalized results are cached per country for a short window so bursts
 * of clients don't hammer upstream.
 */
class FireService(
    private val primary: FireSource,
    private val fallback: FireSource,
    /** Keep only the [maxFires] most intense detections — FIRMS can return thousands of points per
     *  country, which is a heavy payload + map. We rank by Fire Radiative Power and drop the long tail
     *  of low-power detections so clients see the significant hotspots. */
    private val maxFires: Int = DEFAULT_MAX_FIRES,
    private val cacheTtlMillis: Long = 60_000,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val logger = LoggerFactory.getLogger(FireService::class.java)
    private val mutex = Mutex()

    companion object {
        const val DEFAULT_MAX_FIRES = 200
    }

    private data class CacheEntry(val key: String, val at: Long, val data: List<FireResponse>)
    private var cache: CacheEntry? = null

    suspend fun recentFires(country: String = "CO"): List<FireResponse> {
        val code = country.uppercase()
        val bbox = CountryBBoxes.of(code)
        val key = "country=$code"
        cache?.let { if (it.key == key && now() - it.at < cacheTtlMillis) return it.data }

        return mutex.withLock {
            cache?.let { if (it.key == key && now() - it.at < cacheTtlMillis) return it.data }

            // Rank by intensity (Fire Radiative Power) so the cap keeps the strongest fires; recency
            // breaks ties. GDACS events (no FRP) sort after FIRMS points but are few, so they survive.
            val fires = fetchFromSources(bbox)
                .sortedWith(
                    compareByDescending<FireResponse> { it.frpMw ?: -1.0 }
                        .thenByDescending { it.time },
                )
                .take(maxFires)
            cache = CacheEntry(key, now(), fires)
            fires
        }
    }

    private suspend fun fetchFromSources(bbox: BBox): List<FireResponse> {
        runCatching { primary.recentFires(bbox) }
            .onSuccess { if (it.isNotEmpty()) return it }
            .onFailure { logger.warn("Primary fire source ${primary.name} failed; falling back.", it) }

        return runCatching { fallback.recentFires(bbox) }
            .onFailure { logger.error("Fallback fire source ${fallback.name} failed.", it) }
            .getOrDefault(emptyList())
    }
}
