package com.jyodroid.kunasismoayuda.server.routes

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String = "ok", val service: String = "kuna-sismo-ayuda-server")

fun Route.versionRoutes() {
    get("/health") {
        call.respond(HealthResponse())
    }
}
