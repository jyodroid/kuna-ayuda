package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.routes.dto.QuakeResponse
import io.ktor.client.HttpClient

/**
 * Servicio Geológico Colombiano (SGC) — the authoritative national source (last ~200 quakes ≥ M2.0).
 * SGC exposes data via its open-data portal (datos.sgc.gov.co, GeoJSON/WFS). The exact endpoint +
 * schema still need to be pinned against the live portal, so for Milestone 1 this source is a
 * placeholder that reports itself unavailable, causing [QuakeService] to fall back to USGS.
 *
 * TODO(M1 follow-up): wire the real SGC GeoJSON/WFS query here and map it to [QuakeResponse],
 * then it becomes the primary source with USGS as fallback.
 */
class SgcSource(@Suppress("unused") private val client: HttpClient) : QuakeSource {

    override val name: String = "SGC"

    override suspend fun recentQuakes(minMagnitude: Double, bbox: BBox): List<QuakeResponse> {
        // Not yet implemented — return empty so the service falls back to USGS.
        return emptyList()
    }
}
