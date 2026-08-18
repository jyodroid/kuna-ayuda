package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.domain.models.Roles
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext

private fun RoutingContext.callerRole(): String? =
    call.principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()

/** The caller's email (JWT subject), or null. */
internal fun RoutingContext.callerEmail(): String? =
    call.principal<JWTPrincipal>()?.payload?.subject

/** Throws FORBIDDEN unless the caller is an ADMIN or SUPERADMIN. */
internal fun RoutingContext.requireAdmin() {
    if (!Roles.isAdmin(callerRole())) {
        throw appError(ErrorCode.FORBIDDEN, "Administrator privileges required", HttpStatusCode.Forbidden)
    }
}

/** Throws FORBIDDEN unless the caller is a SUPERADMIN (manages other admins). */
internal fun RoutingContext.requireSuperAdmin() {
    if (!Roles.isSuperAdmin(callerRole())) {
        throw appError(ErrorCode.FORBIDDEN, "Super-administrator privileges required", HttpStatusCode.Forbidden)
    }
}
