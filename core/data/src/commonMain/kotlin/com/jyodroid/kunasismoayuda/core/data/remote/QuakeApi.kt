package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class QuakeApi(
    private val client: HttpClient,
    private val baseUrl: String = defaultServerBaseUrl(),
) {
    suspend fun getQuakes(minMagnitude: Double, country: String = "CO"): List<QuakeDto> =
        client.get("$baseUrl/api/quakes") {
            parameter("minMagnitude", minMagnitude)
            parameter("country", country)
        }.body()
}
