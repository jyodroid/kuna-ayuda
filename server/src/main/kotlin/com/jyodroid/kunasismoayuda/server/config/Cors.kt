package com.jyodroid.kunasismoayuda.server.config

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

/** One allowed browser origin, split into the shape Ktor's `allowHost` wants: a bare host + scheme. */
internal data class OriginRule(val host: String, val scheme: String)

/**
 * Parses `ALLOWED_ORIGIN` into [OriginRule]s. Ktor's `allowHost` **rejects a host string that contains a
 * scheme** ("scheme should be specified as a separate parameter") — so we must strip `https://` (and any
 * trailing path) and pass the scheme separately. Accepts a comma-separated list (e.g. the herokuapp host
 * plus a custom domain), a bare host, or a full origin URL.
 */
internal fun parseAllowedOrigins(raw: String?): List<OriginRule> =
    raw?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?.map { origin ->
            val scheme = if (origin.startsWith("http://", ignoreCase = true)) "http" else "https"
            val host = origin.substringAfter("://").substringBefore("/").trim() // drop scheme + any path
            OriginRule(host, scheme)
        }
        ?.filter { it.host.isNotBlank() }
        ?: emptyList()

fun Application.configureCors() {
    val rules = parseAllowedOrigins(System.getenv("ALLOWED_ORIGIN"))
    install(CORS) {
        // In production set ALLOWED_ORIGIN to the app host(s). In dev, allow common localhost ports.
        if (rules.isEmpty()) {
            allowHost("localhost:8080")
            allowHost("localhost:5173")
            anyHost() // dev convenience for KMP clients (desktop/emulator/simulator)
        } else {
            rules.forEach { allowHost(it.host, schemes = listOf(it.scheme)) }
        }

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-App-Key") // app-only gate (see AppGate.kt); lets a future web client preflight
    }
}
