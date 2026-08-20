package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.ShelterRequest
import com.jyodroid.kunasismoayuda.server.services.AuditService
import com.jyodroid.kunasismoayuda.server.services.ShelterService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Moderated shelters/acopios. The list is public (the app reads it); creating or removing a location
 * requires an authenticated ADMIN token. This is the anti-fraud guarantee — the public cannot add
 * or edit locations, only administrators (us) can.
 */
fun Route.shelterRoutes(service: ShelterService, audit: AuditService) = route("/api/shelters") {
    get {
        val country = call.request.queryParameters["country"] ?: "CO"
        call.respond(service.listActive(country))
    }

    authenticate {
        post {
            requireAdmin()
            val request = call.receive<ShelterRequest>()
            val created = service.create(request)
            service.find(created.id)?.let { audit.shelterCreated(actor(), it) }
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            requireAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Shelter id must be an integer", HttpStatusCode.BadRequest)
            val before = service.find(id)
            val request = call.receive<ShelterRequest>()
            val updated = service.update(id, request)
                ?: throw appError(ErrorCode.NOT_FOUND, "Shelter $id not found", HttpStatusCode.NotFound)
            val after = service.find(id)
            if (before != null && after != null) audit.shelterUpdated(actor(), before, after)
            call.respond(updated)
        }

        delete("/{id}") {
            requireAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Shelter id must be an integer", HttpStatusCode.BadRequest)
            val before = service.find(id)
            service.deactivate(id)
            before?.let { audit.shelterDeleted(actor(), it) }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
