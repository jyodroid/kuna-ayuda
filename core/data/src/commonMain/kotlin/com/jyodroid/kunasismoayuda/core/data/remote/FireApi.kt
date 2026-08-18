package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class FireApi(
    private val client: HttpClient,
    private val baseUrl: String = defaultServerBaseUrl(),
) {
    suspend fun getFires(country: String = "CO"): List<FireDto> =
        client.get("$baseUrl/api/fires") {
            parameter("country", country)
        }.body()
}
