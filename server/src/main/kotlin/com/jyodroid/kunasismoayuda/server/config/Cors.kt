package com.jyodroid.kunasismoayuda.server.config

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {
        // In production set ALLOWED_ORIGIN to the app host. In dev, allow common localhost ports.
        val allowedOrigin = System.getenv("ALLOWED_ORIGIN")
        if (allowedOrigin.isNullOrBlank()) {
            allowHost("localhost:8080")
            allowHost("localhost:5173")
            anyHost() // dev convenience for KMP clients (desktop/emulator/simulator)
        } else {
            allowHost(allowedOrigin, schemes = listOf("https"))
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
