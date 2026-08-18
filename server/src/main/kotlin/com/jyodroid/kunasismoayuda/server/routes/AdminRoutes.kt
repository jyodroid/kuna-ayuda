package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.CreateAdminRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.toDto
import com.jyodroid.kunasismoayuda.server.services.AdminService
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
 * Admin-account management — **super-admin only**. This is how the owner (the env-seeded SUPERADMIN)
 * grants or revokes moderator access; ordinary ADMINs can't reach it (requireSuperAdmin).
 */
fun Route.adminRoutes(service: AdminService) = route("/api/admins") {
    authenticate {
        get {
            requireSuperAdmin()
            call.respond(service.list().map { it.toDto() })
        }

        post {
            requireSuperAdmin()
            val request = call.receive<CreateAdminRequest>()
            call.respond(HttpStatusCode.Created, service.create(request.email, request.password).toDto())
        }

        delete("/{id}") {
            requireSuperAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Admin id must be an integer", HttpStatusCode.BadRequest)
            service.delete(id, callerEmail())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
