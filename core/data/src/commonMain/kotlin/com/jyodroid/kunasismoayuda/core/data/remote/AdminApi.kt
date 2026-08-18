package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class AdminDto(val id: Int, val email: String, val role: String)

@Serializable
data class NewAdminDto(val email: String, val password: String)

/** Super-admin-only `/api/admins` calls; all carry the moderator bearer token. */
class AdminApi(
    private val client: HttpClient,
    private val baseUrl: String = defaultServerBaseUrl(),
) {
    suspend fun list(token: String): List<AdminDto> =
        client.get("$baseUrl/api/admins") { bearerAuth(token) }.body()

    suspend fun create(email: String, password: String, token: String): AdminDto =
        client.post("$baseUrl/api/admins") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(NewAdminDto(email, password))
        }.body()

    suspend fun delete(id: Int, token: String) {
        client.delete("$baseUrl/api/admins/$id") { bearerAuth(token) }
    }
}
