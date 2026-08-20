package com.jyodroid.kunasismoayuda.server.routes.dto

import com.jyodroid.kunasismoayuda.server.domain.models.AuditEntry
import com.jyodroid.kunasismoayuda.server.domain.models.ModeratorActivity
import kotlinx.serialization.Serializable

@Serializable
data class AuditEntryDto(
    val id: Int,
    val actorEmail: String,
    val actorRole: String,
    val action: String,
    val entityType: String,
    val entityId: String?,
    val beforeJson: String?,
    val afterJson: String?,
    val ip: String?,
    val createdAt: String,
    val revertedAt: String?,
    val revertedBy: String?,
)

fun AuditEntry.toDto() = AuditEntryDto(
    id, actorEmail, actorRole, action, entityType, entityId, beforeJson, afterJson, ip,
    createdAt.toString(), revertedAt?.toString(), revertedBy,
)

@Serializable
data class ModeratorActivityDto(
    val email: String,
    val role: String,
    val enabled: Boolean,
    val counts: Map<String, Long>,
    val lastActiveAt: String?,
)

fun ModeratorActivity.toDto() = ModeratorActivityDto(email, role, enabled, counts, lastActiveAt)

@Serializable
data class RevertSkipDto(val entryId: Int, val reason: String)

@Serializable
data class RevertAllResponse(val reverted: Int, val skipped: List<RevertSkipDto>)
