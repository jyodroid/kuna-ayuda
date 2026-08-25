package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SosApi(
    private val client: HttpClient,
    private val baseUrl: String = defaultServerBaseUrl(),
) {
    /**
     * Posts a report. Returns the HTTP status code so the caller can tell a permanent rejection
     * (4xx) apart from a transient failure (5xx) worth retrying. A network error (no connectivity)
     * throws — the outbox treats that as transient too.
     */
    suspend fun send(request: SosRequestDto): Int {
        val response: HttpResponse = client.post("$baseUrl/api/sos") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.status.value
    }

    /**
     * Responder view (admin-only): lists submitted reports. [status] filters by "SOS"/"SAFE";
     * null returns both. [archived] false = pending only, true = archived only, null = both.
     * Requires the moderator bearer [token].
     */
    suspend fun listActive(status: String?, archived: Boolean?, token: String): List<SosResponseDto> =
        client.get("$baseUrl/api/sos") {
            bearerAuth(token)
            status?.let { parameter("status", it) }
            parameter("archived", if (archived == null) "all" else archived.toString())
        }.body()

    /** Public reassurance list: named "I'm safe" check-ins for [country] (no auth). */
    suspend fun listPublicSafe(country: String): List<SafeCheckInDto> =
        client.get("$baseUrl/api/sos/safe") { parameter("country", country) }.body()

    /** Dashboard counts (admin-only). */
    suspend fun stats(token: String): SosStatsDto =
        client.get("$baseUrl/api/sos/stats") { bearerAuth(token) }.body()

    /** Archive a report as attended/notified (admin-only). */
    suspend fun markHandled(id: Int, token: String) {
        client.post("$baseUrl/api/sos/$id/handle") { bearerAuth(token) }
    }

    /** Restore an archived report to the active list (admin-only). */
    suspend fun reopen(id: Int, token: String) {
        client.post("$baseUrl/api/sos/$id/reopen") { bearerAuth(token) }
    }

    /** Permanently delete a report (admin-only). */
    suspend fun delete(id: Int, token: String) {
        client.delete("$baseUrl/api/sos/$id") { bearerAuth(token) }
    }
}
