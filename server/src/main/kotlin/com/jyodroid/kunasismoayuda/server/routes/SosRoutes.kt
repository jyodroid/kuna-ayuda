package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.SosRequest
import com.jyodroid.kunasismoayuda.server.services.SosService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Geolocated SOS + "I'm safe" check-in. Submitting is public and deliberately NOT rate-limited —
 * we never want to block someone in danger. Everything else is the moderator responder view:
 * listing, dashboard stats, and the attended/notified lifecycle (archive / reopen / delete).
 */
fun Route.sosRoutes(service: SosService) = route("/api/sos") {
    post {
        val request = call.receive<SosRequest>()
        if (request.status.uppercase() !in SosService.STATUSES) {
            throw appError(ErrorCode.VALIDATION, "status must be SOS or SAFE", HttpStatusCode.BadRequest)
        }
        call.respond(HttpStatusCode.Created, service.create(request))
    }

    authenticate {
        // Responder list. `?status=SOS|SAFE` filters kind (omit/ALL = both); `?archived=true` shows the
        // archived (handled) list, `?archived=all` shows both, absent/`false` = pending only.
        get {
            requireAdmin()
            val archived = when (call.request.queryParameters["archived"]?.lowercase()) {
                "true" -> true
                "all" -> null
                else -> false
            }
            call.respond(service.list(call.request.queryParameters["status"], archived))
        }

        // Pending-vs-handled counts for the responder dashboard.
        get("/stats") {
            requireAdmin()
            call.respond(service.stats())
        }

        // Archive as attended (SOS) / notified (SAFE), stamping the acting moderator.
        post("/{id}/handle") {
            requireAdmin()
            val id = pathId()
            if (!service.markHandled(id, callerEmail())) notFound(id)
            call.respond(HttpStatusCode.NoContent)
        }

        // Restore an archived report to the active list.
        post("/{id}/reopen") {
            requireAdmin()
            val id = pathId()
            if (!service.reopen(id)) notFound(id)
            call.respond(HttpStatusCode.NoContent)
        }

        // Permanently delete (e.g. spam / false alarm).
        delete("/{id}") {
            requireAdmin()
            val id = pathId()
            if (!service.delete(id)) notFound(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun io.ktor.server.routing.RoutingContext.pathId(): Int =
    call.parameters["id"]?.toIntOrNull()
        ?: throw appError(ErrorCode.VALIDATION, "id must be an integer", HttpStatusCode.BadRequest)

private fun io.ktor.server.routing.RoutingContext.notFound(id: Int): Nothing =
    throw appError(ErrorCode.NOT_FOUND, "SOS report $id not found", HttpStatusCode.NotFound)
