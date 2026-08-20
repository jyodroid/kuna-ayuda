package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.domain.models.Actor
import com.jyodroid.kunasismoayuda.server.domain.models.Roles
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext
import org.koin.ktor.ext.get

private fun RoutingContext.callerRole(): String? =
    call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()

/** The caller's email (JWT subject), or null. */
internal fun RoutingContext.callerEmail(): String? =
    call.principal<JWTPrincipal>()?.payload?.subject

/** The real client IP (behind Heroku's proxy: the first `X-Forwarded-For` hop), for the audit log. */
internal fun RoutingContext.clientIp(): String? =
    call.request.headers["X-Forwarded-For"]?.split(",")?.firstOrNull()?.trim()?.ifBlank { null }
        ?: call.request.local.remoteHost

/** Who is acting, for audit records. */
internal fun RoutingContext.actor(): Actor =
    Actor(email = callerEmail() ?: "unknown", role = callerRole() ?: "unknown", ip = clientIp())

/** Throws FORBIDDEN unless the caller is an ADMIN or SUPERADMIN — and their account is still enabled. */
internal fun RoutingContext.requireAdmin() {
    if (!Roles.isAdmin(callerRole())) {
        throw appError(ErrorCode.FORBIDDEN, "Administrator privileges required", HttpStatusCode.Forbidden)
    }
    ensureEnabled()
}

/** Throws FORBIDDEN unless the caller is a SUPERADMIN (manages other admins) — and still enabled. */
internal fun RoutingContext.requireSuperAdmin() {
    if (!Roles.isSuperAdmin(callerRole())) {
        throw appError(ErrorCode.FORBIDDEN, "Super-administrator privileges required", HttpStatusCode.Forbidden)
    }
    ensureEnabled()
}

/**
 * Blocks a moderator whose account has been disabled, even though their 12h JWT is still
 * cryptographically valid — a disabled account can no longer act. One DB lookup per admin request
 * (low volume). Unknown/DB-less callers are left alone (login can't succeed without the DB anyway).
 */
private fun RoutingContext.ensureEnabled() {
    val email = callerEmail() ?: return
    val user = call.application.get<AdminUserRepository>().findByEmail(email)
    if (user != null && !user.enabled) {
        throw appError(ErrorCode.FORBIDDEN, "This moderator account is disabled", HttpStatusCode.Forbidden)
    }
}
