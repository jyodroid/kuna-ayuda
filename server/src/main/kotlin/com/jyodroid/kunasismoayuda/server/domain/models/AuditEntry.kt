package com.jyodroid.kunasismoayuda.server.domain.models

import java.time.LocalDateTime

/** Who performed a moderator action, for the audit trail. */
data class Actor(val email: String, val role: String, val ip: String? = null)

/** The action verbs recorded in the audit log. */
object AuditAction {
    const val SHELTER_CREATE = "SHELTER_CREATE"
    const val SHELTER_UPDATE = "SHELTER_UPDATE"
    const val SHELTER_DELETE = "SHELTER_DELETE"
    const val BOARD_APPROVE = "BOARD_APPROVE"
    const val BOARD_REJECT = "BOARD_REJECT"
    const val SOS_HANDLE = "SOS_HANDLE"
    const val SOS_REOPEN = "SOS_REOPEN"
    const val SOS_DELETE = "SOS_DELETE"
    const val SEARCH_DELETE = "SEARCH_DELETE"
    const val ADMIN_CREATE = "ADMIN_CREATE"
    const val ADMIN_DELETE = "ADMIN_DELETE"
    const val ADMIN_DISABLE = "ADMIN_DISABLE"
    const val ADMIN_ENABLE = "ADMIN_ENABLE"
    const val PASSWORD_CHANGE = "PASSWORD_CHANGE" // self-service
    const val PASSWORD_RESET = "PASSWORD_RESET"   // super-admin reset of another account
    const val LOGIN_SUCCESS = "LOGIN_SUCCESS"
    const val LOGIN_FAILURE = "LOGIN_FAILURE"
    const val REVERT = "REVERT"

    /** Content actions that [com.jyodroid.kunasismoayuda.server.services.AuditService] can undo. */
    val REVERTIBLE = setOf(
        SHELTER_CREATE, SHELTER_UPDATE, SHELTER_DELETE,
        BOARD_APPROVE, BOARD_REJECT,
        SOS_HANDLE, SOS_REOPEN, SOS_DELETE,
        SEARCH_DELETE,
    )
}

/** The entity kinds an audit entry can target. */
object AuditEntity {
    const val SHELTER = "SHELTER"
    const val BOARD_POST = "BOARD_POST"
    const val SOS = "SOS"
    const val SEARCH = "SEARCH"
    const val ADMIN = "ADMIN"
    const val SESSION = "SESSION"
}

/** A recorded moderator action. Snapshots are opaque JSON captured before/after the change. */
data class AuditEntry(
    val id: Int,
    val actorEmail: String,
    val actorRole: String,
    val action: String,
    val entityType: String,
    val entityId: String?,
    val beforeJson: String?,
    val afterJson: String?,
    val ip: String?,
    val createdAt: LocalDateTime,
    val revertedAt: LocalDateTime?,
    val revertedBy: String?,
)

/** Fields to insert a new audit entry. */
data class NewAuditEntry(
    val actorEmail: String,
    val actorRole: String,
    val action: String,
    val entityType: String,
    val entityId: String?,
    val beforeJson: String?,
    val afterJson: String?,
    val ip: String?,
)

/** Filter for the super-admin audit browser. */
data class AuditFilter(
    val actorEmail: String? = null,
    val action: String? = null,
    val entityType: String? = null,
    val reverted: Boolean? = null,
    val limit: Int = 200,
    val offset: Int = 0,
)

/** Per-moderator activity summary for the oversight dashboard. */
data class ModeratorActivity(
    val email: String,
    val role: String,
    val enabled: Boolean,
    val counts: Map<String, Long>, // action -> count (mutations only)
    val lastActiveAt: String?,      // ISO timestamp of the most recent action, or null
)
