package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.routes.dto.QuakeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * USGS FDSN event service, bounded to the requested [BBox]. No API key required; CORS-friendly and
 * updated ~every minute. This is the reliable global fallback source (and primary for non-Colombia
 * countries, which have no SGC).
 * Docs: https://earthquake.usgs.gov/fdsnws/event/1/
 */
class UsgsSource(private val client: HttpClient) : QuakeSource {

    override val name: String = "USGS"

    override suspend fun recentQuakes(minMagnitude: Double, bbox: BBox): List<QuakeResponse> {
        val url = buildString {
            append("https://earthquake.usgs.gov/fdsnws/event/1/query")
            append("?format=geojson")
            append("&minmagnitude=$minMagnitude")
            append("&minlatitude=${bbox.minLat}")
            append("&maxlatitude=${bbox.maxLat}")
            append("&minlongitude=${bbox.minLon}")
            append("&maxlongitude=${bbox.maxLon}")
            append("&orderby=time")
            append("&limit=200")
        }
        val response: UsgsFeatureCollection = client.get(url).body()
        return response.features.mapNotNull { it.toQuakeOrNull() }
    }

    private fun UsgsFeature.toQuakeOrNull(): QuakeResponse? {
        val coords = geometry?.coordinates ?: return null
        if (coords.size < 2) return null
        val lon = coords[0]
        val lat = coords[1]
        val depth = coords.getOrNull(2)
        val time = properties?.time ?: return null
        return QuakeResponse(
            id = id ?: return null,
            time = time,
            magnitude = properties.mag,
            depthKm = depth,
            latitude = lat,
            longitude = lon,
            place = properties.place ?: "Desconocido",
            source = name,
            url = properties.url,
        )
    }
}

@Serializable
private data class UsgsFeatureCollection(val features: List<UsgsFeature> = emptyList())

@Serializable
private data class UsgsFeature(
    val id: String? = null,
    val properties: UsgsProperties? = null,
    val geometry: UsgsGeometry? = null,
)

@Serializable
private data class UsgsProperties(
    val mag: Double? = null,
    val place: String? = null,
    val time: Long? = null,
    val url: String? = null,
)

@Serializable
private data class UsgsGeometry(
    @SerialName("coordinates") val coordinates: List<Double> = emptyList(),
)
