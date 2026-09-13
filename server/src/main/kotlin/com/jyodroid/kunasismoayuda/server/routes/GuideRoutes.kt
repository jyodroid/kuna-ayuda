package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.content.GuideContent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * Public emergency-guide endpoint — the central source of truth for emergency numbers + safety tips,
 * so every platform shares one copy. `?country=CO|ID|ES|IT|PE` (default CO) picks the numbers;
 * `?lang=es|en` (default es) picks the tips language. Static content; no DB, no auth.
 */
fun Route.guideRoutes() {
    get("/api/guide") {
        val country = call.request.queryParameters["country"] ?: "CO"
        val lang = call.request.queryParameters["lang"] ?: "es"
        call.respond(GuideContent.response(country, lang))
    }
}
