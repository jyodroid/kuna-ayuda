package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.config.LoginRateLimit
import com.jyodroid.kunasismoayuda.server.domain.models.AuditAction
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.ChangePasswordRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.DeleteAccountRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.LoginRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.LoginResponse
import com.jyodroid.kunasismoayuda.server.services.AuditService
import com.jyodroid.kunasismoayuda.server.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Moderator login (public but rate-limited) + self-service password change (authenticated). Login is
 * the only place a JWT is minted; both outcomes are recorded in the audit log. Password change lets any
 * moderator update their own password from the web console.
 */
fun Route.authRoutes(service: AuthService, audit: AuditService) = route("/api/auth") {
    rateLimit(LoginRateLimit) {
        post("/login") {
            val request = call.receive<LoginRequest>()
            if (request.email.isBlank() || request.password.isBlank()) {
                throw appError(ErrorCode.VALIDATION, "email and password are required", HttpStatusCode.BadRequest)
            }
            val email = request.email.trim().lowercase()
            val result = try {
                service.login(request.email, request.password)
            } catch (e: Throwable) {
                audit.login(email, role = "unknown", ip = clientIp(), success = false)
                throw e
            }
            audit.login(email, result.role, clientIp(), success = true)
            call.respond(LoginResponse(token = result.token, role = result.role))
        }
    }

    authenticate {
        // Self-service: any logged-in moderator changes their own password (current password required).
        post("/password") {
            requireAdmin()
            val req = call.receive<ChangePasswordRequest>()
            val email = callerEmail()
                ?: throw appError(ErrorCode.UNAUTHORIZED, "Not authenticated", HttpStatusCode.Unauthorized)
            service.changeOwnPassword(email, req.currentPassword, req.newPassword)
            audit.adminEvent(actor(), AuditAction.PASSWORD_CHANGE, adminId = null, note = "self")
            call.respond(HttpStatusCode.NoContent)
        }

        // Self-service: a moderator deletes their own account (current password required). The
        // SUPERADMIN owner is refused (403). Their token stops working immediately afterwards.
        post("/account/delete") {
            requireAdmin()
            val req = call.receive<DeleteAccountRequest>()
            val email = callerEmail()
                ?: throw appError(ErrorCode.UNAUTHORIZED, "Not authenticated", HttpStatusCode.Unauthorized)
            val deleted = service.deleteOwnAccount(email, req.currentPassword)
            audit.adminEvent(actor(), AuditAction.ADMIN_DELETE, adminId = deleted.id, note = "self")
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
