package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    /**
     * @param onUnauthorized invoked whenever an **authenticated** request (one carrying a Bearer
     * token) comes back **401** — i.e. the moderator's JWT expired or was revoked. The app wires this
     * to clear the session so the UI drops back to the login screen automatically. It fires only when
     * the request had an `Authorization` header, so a public-endpoint 401 (e.g. bad login credentials
     * or the app-gate) never clears a session that isn't there.
     */
    fun create(onUnauthorized: () -> Unit = {}): HttpClient = HttpClient {
        installKunaDefaults(onUnauthorized)
    }
}

/**
 * The shared client configuration (content negotiation, app-key header, and the 401 auto-logout
 * validator). Extracted so tests can apply the exact same config over a `MockEngine`.
 */
internal fun HttpClientConfig<*>.installKunaDefaults(onUnauthorized: () -> Unit) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }
    install(Logging) {
        level = LogLevel.INFO
    }
    // Identifies the caller as one of our apps to the server's optional app-only gate (AppGate.kt).
    install(DefaultRequest) {
        headers.append("X-App-Key", APP_CLIENT_KEY)
    }
    // Auto-logout: a 401 on a token-bearing request means the moderator session is no longer valid.
    HttpResponseValidator {
        validateResponse { response ->
            if (response.status == HttpStatusCode.Unauthorized &&
                response.call.request.headers.contains(HttpHeaders.Authorization)
            ) {
                onUnauthorized()
            }
        }
    }
}
