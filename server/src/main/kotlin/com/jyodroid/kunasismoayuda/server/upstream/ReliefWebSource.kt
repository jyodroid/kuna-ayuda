package com.jyodroid.kunasismoayuda.server.upstream

import com.jyodroid.kunasismoayuda.server.domain.models.DisasterReport
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime

/**
 * ReliefWeb (UN OCHA) reports API v2 — free JSON, but requires a ReliefWeb-**approved** `appname`
 * (arbitrary names get HTTP 403; the old v1 that accepted any name is decommissioned). Register one at
 * https://apidoc.reliefweb.int/parameters#appname and set it via `RELIEFWEB_APPNAME`. Until then this
 * source returns an empty list (the ingestion job isolates it, so GDACS still works).
 * We POST a JSON query (avoids URL-encoding the bracketed params) for recent "Colombia earthquake"
 * situation reports. Docs: https://apidoc.reliefweb.int/
 */
class ReliefWebSource(
    private val client: HttpClient,
    private val appName: String = System.getenv("RELIEFWEB_APPNAME") ?: "kuna-sismo-ayuda",
) {
    val name: String = "ReliefWeb"

    suspend fun recentReports(limit: Int = 20): List<DisasterReport> {
        val request = RwRequest(
            query = RwQuery(value = "Colombia earthquake", operator = "AND"),
            sort = listOf("date:desc"),
            limit = limit,
            fields = RwFields(
                include = listOf(
                    "title", "url", "date.created", "source.name",
                    "primary_country.name", "disaster_type.name", "body-html",
                ),
            ),
        )
        val response: RwResponse = client.post("https://api.reliefweb.int/v2/reports?appname=$appName") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.data.mapNotNull { it.toReportOrNull() }
    }

    private fun RwItem.toReportOrNull(): DisasterReport? {
        val extId = id?.toString() ?: return null
        val f = fields ?: return null
        val title = f.title ?: return null
        return DisasterReport(
            source = name,
            externalId = extId,
            title = title,
            body = f.bodyHtml?.let(::htmlExcerpt),
            orgSource = f.source.firstOrNull()?.name,
            country = f.primaryCountry?.name,
            disasterType = f.disasterType.firstOrNull()?.name,
            url = f.url,
            publishedAt = f.date?.created?.let(::parseIsoOffset),
            fetchedAt = Instant.now(),
        )
    }

    // Strip tags and collapse whitespace into a short plain-text excerpt (bodies can be very long).
    private fun htmlExcerpt(html: String): String =
        html.replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim().take(1000)

    private fun parseIsoOffset(raw: String): Instant? =
        runCatching { OffsetDateTime.parse(raw).toInstant() }.getOrNull()
}

// --- Request DTOs ---
@Serializable
private data class RwRequest(
    val query: RwQuery,
    val sort: List<String>,
    val limit: Int,
    val fields: RwFields,
)

@Serializable
private data class RwQuery(val value: String, val operator: String)

@Serializable
private data class RwFields(val include: List<String>)

// --- Response DTOs ---
@Serializable
private data class RwResponse(val data: List<RwItem> = emptyList())

@Serializable
private data class RwItem(val id: Long? = null, val fields: RwFieldsData? = null)

@Serializable
private data class RwFieldsData(
    val title: String? = null,
    val url: String? = null,
    val date: RwDate? = null,
    val source: List<RwNamed> = emptyList(),
    @SerialName("primary_country") val primaryCountry: RwNamed? = null,
    @SerialName("disaster_type") val disasterType: List<RwNamed> = emptyList(),
    @SerialName("body-html") val bodyHtml: String? = null,
)

@Serializable
private data class RwDate(val created: String? = null)

@Serializable
private data class RwNamed(val name: String? = null)
