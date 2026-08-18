package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.domain.repositories.DisasterRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ReportRepository
import com.jyodroid.kunasismoayuda.server.routes.dto.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

/**
 * Public read endpoints for the ingested feeds (GDACS disasters + ReliefWeb reports). Each item
 * carries `fetchedAt` (epoch millis) so the client can show how fresh the data is — critical for a
 * disaster app. Writes happen only via the ingestion job, never from clients.
 */
fun Route.disasterRoutes(disasterRepo: DisasterRepository, reportRepo: ReportRepository) {
    get("/api/disasters") {
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
        call.respond(disasterRepo.listRecent(limit).map { it.toDto() })
    }

    get("/api/disaster-reports") {
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 50
        call.respond(reportRepo.listRecent(limit).map { it.toDto() })
    }
}
