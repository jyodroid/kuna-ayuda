package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.domain.models.Disaster
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * GDACS (Global Disaster Alert & Coordination System) event list. Public, no API key, GeoJSON.
 * We keep only earthquakes (EQ) that fall in Colombia (ISO3 == COL, or inside [ColombiaBBox]).
 *
 * Uses the SEARCH endpoint with a rolling [windowDays] date range — the "EVENTS4APP" feed only holds
 * the ~100 most-current global events (mostly wildfires), so a significant Colombian quake ages out of
 * it within days. SEARCH lets us look back far enough to still surface it.
 * Docs: https://www.gdacs.org/gdacsapi/
 */
class GdacsSource(
    private val client: HttpClient,
    private val windowDays: Long = 30,
) {
    val name: String = "GDACS"

    suspend fun recentEarthquakes(): List<Disaster> {
        val today = LocalDate.now(ZoneOffset.UTC)
        val from = today.minusDays(windowDays)
        val url = "https://www.gdacs.org/gdacsapi/api/events/geteventlist/SEARCH" +
            "?eventlist=EQ&fromdate=$from&todate=$today"
        val fc: GdacsFeatureCollection = client.get(url).body()
        return fc.features.mapNotNull { it.toDisasterOrNull() }
    }

    private fun GdacsFeature.toDisasterOrNull(): Disaster? {
        val p = properties ?: return null
        if (!p.eventtype.equals("EQ", ignoreCase = true)) return null

        val coords = geometry?.coordinates
        val lon = coords?.getOrNull(0)
        val lat = coords?.getOrNull(1)
        val inColombia = p.iso3.equals("COL", ignoreCase = true) ||
            (lat != null && lon != null && ColombiaBBox.contains(lat, lon))
        if (!inColombia) return null

        val eventId = p.eventid?.toString() ?: return null
        return Disaster(
            source = name,
            externalId = eventId,
            eventType = "EQ",
            title = p.name ?: p.description ?: "Sismo",
            description = p.htmldescription ?: p.description,
            country = p.country,
            iso3 = p.iso3,
            latitude = lat,
            longitude = lon,
            magnitude = p.severitydata?.severity,
            alertLevel = p.alertlevel,
            severityText = p.severitydata?.severitytext,
            eventDate = p.fromdate?.let(::parseGdacsDate),
            url = p.url?.report ?: p.url?.details,
            fetchedAt = Instant.now(),
        )
    }

    // GDACS uses local naive ISO like "2026-08-14T16:56:14" (UTC). Defensive: null if unparseable.
    private fun parseGdacsDate(raw: String): Instant? =
        runCatching { LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC) }.getOrNull()
}

@Serializable
private data class GdacsFeatureCollection(val features: List<GdacsFeature> = emptyList())

@Serializable
private data class GdacsFeature(
    val properties: GdacsProperties? = null,
    val geometry: GdacsGeometry? = null,
)

@Serializable
private data class GdacsGeometry(val coordinates: List<Double> = emptyList())

@Serializable
private data class GdacsProperties(
    val eventtype: String? = null,
    val eventid: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val htmldescription: String? = null,
    val alertlevel: String? = null,
    val country: String? = null,
    val iso3: String? = null,
    val fromdate: String? = null,
    val severitydata: GdacsSeverity? = null,
    val url: GdacsUrl? = null,
)

@Serializable
private data class GdacsSeverity(
    val severity: Double? = null,
    val severitytext: String? = null,
)

@Serializable
private data class GdacsUrl(
    val report: String? = null,
    val details: String? = null,
)
