package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.routes.dto.FireResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * GDACS wildfire (WF) events, filtered to the requested [BBox]. Public, no API key — the keyless
 * fallback for [FireService] when no FIRMS `MAP_KEY` is set. GDACS is event-level (a handful of large
 * fire complexes) rather than a dense point feed, so it fills [FireResponse.frpMw]=null and carries the
 * alert level in [FireResponse.confidence] plus a human [FireResponse.place].
 *
 * Uses the **EVENTS4APP** endpoint — the ~100 most-current global events, which GDACS populates mostly
 * with wildfires (the `SEARCH?eventlist=WF` endpoint returns 204 No Content, so it can't be used here).
 */
class GdacsFireSource(
    private val client: HttpClient,
) : FireSource {

    override val name: String = "GDACS"
    private val logger = LoggerFactory.getLogger(GdacsFireSource::class.java)

    override suspend fun recentFires(bbox: BBox): List<FireResponse> {
        val url = "https://www.gdacs.org/gdacsapi/api/events/geteventlist/EVENTS4APP"
        val fc = runCatching { client.get(url).body<GdacsWfCollection>() }
            .getOrElse {
                logger.warn("GDACS wildfire request failed", it)
                return emptyList()
            }
        return fc.features.mapNotNull { it.toFireOrNull(bbox) }
    }

    private fun GdacsWfFeature.toFireOrNull(bbox: BBox): FireResponse? {
        val p = properties ?: return null
        if (!p.eventtype.equals("WF", ignoreCase = true)) return null
        val coords = geometry?.coordinates
        val lon = coords?.getOrNull(0) ?: return null
        val lat = coords.getOrNull(1) ?: return null
        if (!bbox.contains(lat, lon)) return null
        val eventId = p.eventid?.toString() ?: return null
        return FireResponse(
            id = "gdacs:$eventId",
            time = p.fromdate?.let(::parseGdacsDate) ?: System.currentTimeMillis(),
            latitude = lat,
            longitude = lon,
            brightnessK = null,
            frpMw = null,
            confidence = p.alertlevel,
            daynight = null,
            source = name,
            place = p.name ?: p.country ?: p.description,
        )
    }

    private fun parseGdacsDate(raw: String): Long? =
        runCatching { LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC).toEpochMilli() }.getOrNull()
}

@Serializable
private data class GdacsWfCollection(val features: List<GdacsWfFeature> = emptyList())

@Serializable
private data class GdacsWfFeature(
    val properties: GdacsWfProperties? = null,
    val geometry: GdacsWfGeometry? = null,
)

@Serializable
private data class GdacsWfGeometry(val coordinates: List<Double> = emptyList())

@Serializable
private data class GdacsWfProperties(
    val eventtype: String? = null,
    val eventid: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val alertlevel: String? = null,
    val country: String? = null,
    val fromdate: String? = null,
)
