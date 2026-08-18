package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ShelterApi(
    private val client: HttpClient,
    private val baseUrl: String = defaultServerBaseUrl(),
) {
    suspend fun getShelters(country: String = "CO"): List<ShelterDto> =
        client.get("$baseUrl/api/shelters") { parameter("country", country) }.body()

    /** Admin-only: the server enforces role=ADMIN; we attach the bearer token. */
    suspend fun create(shelter: NewShelterDto, token: String): ShelterDto =
        client.post("$baseUrl/api/shelters") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(shelter)
        }.body()

    suspend fun update(id: Int, shelter: NewShelterDto, token: String): ShelterDto =
        client.put("$baseUrl/api/shelters/$id") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(shelter)
        }.body()

    suspend fun delete(id: Int, token: String) {
        client.delete("$baseUrl/api/shelters/$id") { bearerAuth(token) }
    }
}
