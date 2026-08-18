package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.ShelterRequest
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
fun Route.shelterRoutes(service: ShelterService) = route("/api/shelters") {
    get {
        val country = call.request.queryParameters["country"] ?: "CO"
        call.respond(service.listActive(country))
    }

    authenticate {
        post {
            requireAdmin()
            val request = call.receive<ShelterRequest>()
            call.respond(HttpStatusCode.Created, service.create(request))
        }

        put("/{id}") {
            requireAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Shelter id must be an integer", HttpStatusCode.BadRequest)
            val request = call.receive<ShelterRequest>()
            val updated = service.update(id, request)
                ?: throw appError(ErrorCode.NOT_FOUND, "Shelter $id not found", HttpStatusCode.NotFound)
            call.respond(updated)
        }

        delete("/{id}") {
            requireAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Shelter id must be an integer", HttpStatusCode.BadRequest)
            service.deactivate(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
