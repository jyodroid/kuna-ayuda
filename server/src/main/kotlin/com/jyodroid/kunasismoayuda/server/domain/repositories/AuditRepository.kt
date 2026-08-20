package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.AuditEntry
import com.jyodroid.kunasismoayuda.server.domain.models.AuditFilter
import com.jyodroid.kunasismoayuda.server.domain.models.NewAuditEntry

interface AuditRepository {
    /** Append an audit entry. Returns the new id, or null when no DB is configured. */
    fun record(entry: NewAuditEntry): Int?

    fun findById(id: Int): AuditEntry?

    /** Filtered, newest-first, paginated list for the audit browser. */
    fun list(filter: AuditFilter): List<AuditEntry>

    /** Non-reverted revertible entries by one moderator, newest-first (for revert-all). */
    fun listRevertibleByActor(email: String): List<AuditEntry>

    /** The most recent audit entry that touched a given entity (for revert conflict detection). */
    fun latestForEntity(entityType: String, entityId: String): AuditEntry?

    /** Stamp an entry as reverted. Returns true if it existed and wasn't already reverted. */
    fun markReverted(id: Int, by: String): Boolean

    /** Per-(actor, action) mutation counts + last-active timestamp, for the moderator dashboard. */
    fun actorActionCounts(): List<ActorActionCount>
}

/** Aggregate row: one moderator's count of a single action, plus their overall last-active time. */
data class ActorActionCount(
    val actorEmail: String,
    val action: String,
    val count: Long,
    val lastActiveAt: String?,
)
