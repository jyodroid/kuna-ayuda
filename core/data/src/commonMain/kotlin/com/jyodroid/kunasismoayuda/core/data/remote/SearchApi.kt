package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class SearchApi(
    private val client: HttpClient,
    private val baseUrl: String = defaultServerBaseUrl(),
) {
    suspend fun list(subject: String?, state: String?, country: String = "CO"): List<SearchReportDto> =
        client.get("$baseUrl/api/search") {
            subject?.let { parameter("subject", it) }
            state?.let { parameter("state", it) }
            parameter("country", country)
        }.body()

    suspend fun create(dto: NewSearchReportDto): SearchReportDto =
        client.post("$baseUrl/api/search") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    /** Upload a single image; returns its stored id to attach to a report. */
    suspend fun uploadPhoto(bytes: ByteArray, mime: String): PhotoUploadDto =
        client.post("$baseUrl/api/photos") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = bytes,
                            headers = io.ktor.http.Headers.build {
                                append(HttpHeaders.ContentType, mime)
                                append(HttpHeaders.ContentDisposition, "filename=\"photo\"")
                            },
                        )
                    },
                ),
            )
        }.body()

    /** Moderator-only: close an abusive report. */
    suspend fun delete(id: Int, token: String) {
        client.delete("$baseUrl/api/search/$id") { bearerAuth(token) }
    }
}
