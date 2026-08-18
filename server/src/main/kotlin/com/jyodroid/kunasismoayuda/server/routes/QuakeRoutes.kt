package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.services.QuakeService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Public earthquake feed. No auth in M1.
 * GET /api/quakes?minMagnitude=2.5&country=CO
 */
fun Route.quakeRoutes(quakeService: QuakeService) = route("/api/quakes") {
    get {
        val minMagnitude = call.request.queryParameters["minMagnitude"]?.toDoubleOrNull() ?: 2.5
        val country = call.request.queryParameters["country"] ?: "CO"
        val quakes = quakeService.recentQuakes(minMagnitude, country)
        call.respond(quakes)
    }
}
