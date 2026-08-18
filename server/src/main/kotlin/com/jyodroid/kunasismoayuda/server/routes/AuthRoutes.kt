package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.config.LoginRateLimit
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.LoginRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.LoginResponse
import com.jyodroid.kunasismoayuda.server.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Moderator login. This is public (you must be able to reach it before you have a token) but
 * rate-limited to blunt credential-stuffing. It only ever issues tokens for `admin_users` accounts;
 * everyone else uses the app anonymously and never touches this route.
 */
fun Route.authRoutes(service: AuthService) = route("/api/auth") {
    rateLimit(LoginRateLimit) {
        post("/login") {
            val request = call.receive<LoginRequest>()
            if (request.email.isBlank() || request.password.isBlank()) {
                throw appError(ErrorCode.VALIDATION, "email and password are required", HttpStatusCode.BadRequest)
            }
            val result = service.login(request.email, request.password)
            call.respond(LoginResponse(token = result.token, role = result.role))
        }
    }
}
