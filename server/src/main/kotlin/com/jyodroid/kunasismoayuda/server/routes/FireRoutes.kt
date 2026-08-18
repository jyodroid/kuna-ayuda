package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.services.FireService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Public active-wildfire feed — the second-hazard parallel of [quakeRoutes]. No auth.
 * GET /api/fires?country=CO|ID|ES|IT
 */
fun Route.fireRoutes(fireService: FireService) = route("/api/fires") {
    get {
        val country = call.request.queryParameters["country"] ?: "CO"
        call.respond(fireService.recentFires(country))
    }
}
