package com.jyodroid.kunasismoayuda.server.routes

import com.jyodroid.kunasismoayuda.server.domain.models.AuditFilter
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import com.jyodroid.kunasismoayuda.server.routes.dto.RevertAllResponse
import com.jyodroid.kunasismoayuda.server.routes.dto.RevertSkipDto
import com.jyodroid.kunasismoayuda.server.routes.dto.toDto
import com.jyodroid.kunasismoayuda.server.services.AuditService
import com.jyodroid.kunasismoayuda.server.services.RevertOutcome
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Super-admin oversight ("monitor the monitors"): browse the audit trail, see per-moderator activity,
 * and undo a single change or all of one moderator's changes. Everything here is SUPERADMIN-only.
 */
fun Route.auditRoutes(service: AuditService) = route("/api/audit") {
    authenticate {
        get {
            requireSuperAdmin()
            val q = call.request.queryParameters
            val filter = AuditFilter(
                actorEmail = q["actor"]?.ifBlank { null },
                action = q["action"]?.ifBlank { null },
                entityType = q["entity"]?.ifBlank { null },
                reverted = q["reverted"]?.toBooleanStrictOrNull(),
                limit = q["limit"]?.toIntOrNull() ?: 200,
                offset = q["offset"]?.toIntOrNull() ?: 0,
            )
            call.respond(service.list(filter).map { it.toDto() })
        }

        get("/moderators") {
            requireSuperAdmin()
            call.respond(service.moderators().map { it.toDto() })
        }

        post("/{id}/revert") {
            requireSuperAdmin()
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw appError(ErrorCode.VALIDATION, "Audit id must be an integer", HttpStatusCode.BadRequest)
            when (service.revert(id, actor())) {
                RevertOutcome.REVERTED -> call.respond(HttpStatusCode.NoContent)
                RevertOutcome.NOT_FOUND ->
                    throw appError(ErrorCode.NOT_FOUND, "Audit entry $id not found", HttpStatusCode.NotFound)
                RevertOutcome.ALREADY_REVERTED ->
                    throw appError(ErrorCode.VALIDATION, "Already reverted", HttpStatusCode.Conflict)
                RevertOutcome.NOT_REVERTIBLE ->
                    throw appError(ErrorCode.VALIDATION, "This action can't be reverted", HttpStatusCode.UnprocessableEntity)
                RevertOutcome.FAILED ->
                    throw appError(ErrorCode.INTERNAL, "Revert failed", HttpStatusCode.InternalServerError)
            }
        }

        post("/revert-all") {
            requireSuperAdmin()
            val email = call.request.queryParameters["moderator"]?.ifBlank { null }
                ?: throw appError(ErrorCode.VALIDATION, "moderator query parameter is required", HttpStatusCode.BadRequest)
            val result = service.revertAllByModerator(email, actor())
            call.respond(RevertAllResponse(result.reverted, result.skipped.map { RevertSkipDto(it.entryId, it.reason) }))
        }
    }
}
