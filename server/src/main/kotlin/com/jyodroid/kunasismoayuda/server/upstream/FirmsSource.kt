package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.routes.dto.FireResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * NASA FIRMS active-fire / thermal-anomaly points (VIIRS/MODIS), bounded to the requested [BBox].
 * The wildfire analog of [UsgsSource]: a dense per-point live feed. The **Area CSV API** returns rows
 * of `latitude,longitude,bright_ti4,scan,track,acq_date,acq_time,satellite,instrument,confidence,
 * version,bright_ti5,frp,daynight` (column order varies slightly by sensor, so we resolve columns by
 * header name rather than by index).
 *
 * Requires a **free `MAP_KEY`** ([mapKey], from the `FIRMS_MAP_KEY` env). When it's blank the source is
 * disabled and returns an empty list — the aggregating [com.jyodroid.kunasismoayuda.server.services.FireService]
 * then falls back to keyless GDACS, so the server runs fine without a key (like the other env-gated
 * integrations). Docs: https://firms.modaps.eosdis.nasa.gov/api/area/
 */
class FirmsSource(
    private val client: HttpClient,
    private val mapKey: String?,
    /** VIIRS S-NPP near-real-time: good global coverage + FRP. Overridable for MODIS/NOAA-20. */
    private val sensor: String = "VIIRS_SNPP_NRT",
    /** Rolling window in days (FIRMS Area API allows 1–10). */
    private val dayRange: Int = 2,
) : FireSource {

    override val name: String = "FIRMS ($sensor)"
    private val logger = LoggerFactory.getLogger(FirmsSource::class.java)

    val isEnabled: Boolean get() = !mapKey.isNullOrBlank()

    override suspend fun recentFires(bbox: BBox): List<FireResponse> {
        val key = mapKey?.takeIf { it.isNotBlank() } ?: return emptyList()
        // FIRMS area order is west,south,east,north = minLon,minLat,maxLon,maxLat.
        val area = "${bbox.minLon},${bbox.minLat},${bbox.maxLon},${bbox.maxLat}"
        val url = "https://firms.modaps.eosdis.nasa.gov/api/area/csv/$key/$sensor/$area/$dayRange"

        val csv = runCatching { client.get(url).bodyAsText() }
            .getOrElse {
                logger.warn("FIRMS request failed for $sensor", it)
                return emptyList()
            }
        return parseCsv(csv)
    }

    private fun parseCsv(csv: String): List<FireResponse> {
        val lines = csv.lineSequence().filter { it.isNotBlank() }.toList()
        if (lines.size < 2) return emptyList()
        // The API returns an error/quota message as plain text (no commas) instead of a CSV header.
        val header = lines.first().split(',').map { it.trim().lowercase() }
        if (!header.contains("latitude") || !header.contains("longitude")) {
            logger.warn("FIRMS returned a non-CSV body: ${lines.first().take(120)}")
            return emptyList()
        }
        val idx = header.withIndex().associate { (i, name) -> name to i }
        fun cols(row: List<String>, name: String): String? = idx[name]?.let { row.getOrNull(it)?.trim() }

        return lines.drop(1).mapNotNull { line ->
            val row = line.split(',')
            val lat = cols(row, "latitude")?.toDoubleOrNull() ?: return@mapNotNull null
            val lon = cols(row, "longitude")?.toDoubleOrNull() ?: return@mapNotNull null
            val acqDate = cols(row, "acq_date")
            val acqTime = cols(row, "acq_time")
            val satellite = cols(row, "satellite") ?: sensor
            val brightness = cols(row, "bright_ti4")?.toDoubleOrNull()
                ?: cols(row, "brightness")?.toDoubleOrNull()
            FireResponse(
                id = "firms:$lat,$lon,${acqDate ?: ""},${acqTime ?: ""},$satellite",
                time = toEpochMillis(acqDate, acqTime),
                latitude = lat,
                longitude = lon,
                brightnessK = brightness,
                frpMw = cols(row, "frp")?.toDoubleOrNull(),
                confidence = cols(row, "confidence"),
                daynight = cols(row, "daynight"),
                source = name,
                place = null,
            )
        }
    }

    /** FIRMS `acq_date` = "YYYY-MM-DD", `acq_time` = UTC "HHmm" (may be 1–4 digits). */
    private fun toEpochMillis(acqDate: String?, acqTime: String?): Long {
        if (acqDate == null) return System.currentTimeMillis()
        return runCatching {
            val date = LocalDate.parse(acqDate)
            val padded = (acqTime ?: "0").padStart(4, '0')
            val time = LocalTime.of(padded.substring(0, 2).toInt(), padded.substring(2, 4).toInt())
            LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC).toEpochMilli()
        }.getOrDefault(System.currentTimeMillis())
    }
}
