package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.config.BoardRateLimit
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.SearchReportRequest
import com.jyodroid.kunasismoayuda.server.services.AuditService
import com.jyodroid.kunasismoayuda.server.services.SearchService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Lost & Found / reunification board (pets + people). Reading and posting are public — anyone can
 * report a lost pet/person or a found one. Posts publish directly (no moderation), rate-limited +
 * validated; administrators can close abusive entries. Photos are uploaded separately via `/api/photos`.
 */
fun Route.searchRoutes(service: SearchService, audit: AuditService) = route("/api/search") {
    get {
        val subject = call.request.queryParameters["subject"]?.uppercase()
        val state = call.request.queryParameters["state"]?.uppercase()
        val country = call.request.queryParameters["country"] ?: "CO"
        call.respond(service.list(subject, state, country))
    }

    rateLimit(BoardRateLimit) {
        post {
            val request = call.receive<SearchReportRequest>()
            call.respond(HttpStatusCode.Created, service.create(request))
        }
    }

    authenticate {
        // Admin: close an abusive report.
        delete("/{id}") {
            requireAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Report id must be an integer", HttpStatusCode.BadRequest)
            val before = service.find(id)
            service.close(id)
            before?.let { audit.searchDeleted(actor(), it) }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
