package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.domain.models.AuditAction
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.CreateAdminRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.ResetPasswordRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.toDto
import com.jyodroid.kunasismoayuda.server.services.AdminService
import com.jyodroid.kunasismoayuda.server.services.AuditService
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
 * Admin-account management — **super-admin only**. Grant/revoke moderator access, disable/enable an
 * account without deleting it (keeps the audit trail), and reset a moderator's password. Every action
 * is recorded in the audit log. Ordinary ADMINs can't reach any of this (requireSuperAdmin).
 */
fun Route.adminRoutes(service: AdminService, audit: AuditService) = route("/api/admins") {
    authenticate {
        get {
            requireSuperAdmin()
            call.respond(service.list().map { it.toDto() })
        }

        post {
            requireSuperAdmin()
            val request = call.receive<CreateAdminRequest>()
            val created = service.create(request.email, request.password)
            audit.adminEvent(actor(), AuditAction.ADMIN_CREATE, created.id, note = created.email)
            call.respond(HttpStatusCode.Created, created.toDto())
        }

        delete("/{id}") {
            requireSuperAdmin()
            val id = pathId()
            service.delete(id, callerEmail())
            audit.adminEvent(actor(), AuditAction.ADMIN_DELETE, id)
            call.respond(HttpStatusCode.NoContent)
        }

        // Disable a moderator (their existing JWT stops working immediately — see requireAdmin()).
        post("/{id}/disable") {
            requireSuperAdmin()
            val id = pathId()
            val target = service.setEnabled(id, enabled = false, callerEmail = callerEmail())
            audit.adminEvent(actor(), AuditAction.ADMIN_DISABLE, id, note = target.email)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/{id}/enable") {
            requireSuperAdmin()
            val id = pathId()
            val target = service.setEnabled(id, enabled = true, callerEmail = callerEmail())
            audit.adminEvent(actor(), AuditAction.ADMIN_ENABLE, id, note = target.email)
            call.respond(HttpStatusCode.NoContent)
        }

        // Super-admin reset of another moderator's password (no current password required).
        post("/{id}/password") {
            requireSuperAdmin()
            val id = pathId()
            val req = call.receive<ResetPasswordRequest>()
            val target = service.resetPassword(id, req.newPassword)
            audit.adminEvent(actor(), AuditAction.PASSWORD_RESET, id, note = target.email)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun io.ktor.server.routing.RoutingContext.pathId(): Int =
    call.parameters["id"]?.toIntOrNull()
        ?: throw appError(ErrorCode.VALIDATION, "Admin id must be an integer", HttpStatusCode.BadRequest)
