package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.Actor
import com.jyodroid.kunasismoayuda.server.domain.models.AuditAction
import com.jyodroid.kunasismoayuda.server.domain.models.AuditEntity
import com.jyodroid.kunasismoayuda.server.domain.models.AuditEntry
import com.jyodroid.kunasismoayuda.server.domain.models.AuditFilter
import com.jyodroid.kunasismoayuda.server.domain.models.ModeratorActivity
import com.jyodroid.kunasismoayuda.server.domain.models.NewAuditEntry
import com.jyodroid.kunasismoayuda.server.domain.models.ResourcePost
import com.jyodroid.kunasismoayuda.server.domain.models.SearchReport
import com.jyodroid.kunasismoayuda.server.domain.models.Shelter
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.AuditRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ShelterRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/** The result of a single revert. */
enum class RevertOutcome { REVERTED, NOT_FOUND, ALREADY_REVERTED, NOT_REVERTIBLE, FAILED }

/** One entry skipped during a bulk revert, with why. */
data class RevertSkip(val entryId: Int, val reason: String)

/** The result of reverting all of a moderator's changes. */
data class RevertAllResult(val reverted: Int, val skipped: List<RevertSkip>)

/**
 * "Monitor the monitors": records every moderator mutation (who/what/before/after/ip) and can undo a
 * single change or all of a moderator's changes. Reverts restore editable state from the stored
 * snapshots; a bulk revert skips entries whose entity was later touched by someone else (conflict).
 * Recording is best-effort — an audit failure never breaks the underlying moderation action.
 */
class AuditService(
    private val audit: AuditRepository,
    private val adminUsers: AdminUserRepository,
    private val shelters: ShelterRepository,
    private val board: ResourceBoardRepository,
    private val sos: SosRepository,
    private val search: SearchRepository,
) {
    private val log = LoggerFactory.getLogger(AuditService::class.java)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ---- recording (typed helpers used by the routes) ---------------------------------------------

    private fun record(
        actor: Actor,
        action: String,
        entityType: String,
        entityId: String?,
        beforeJson: String? = null,
        afterJson: String? = null,
    ) {
        runCatching {
            audit.record(
                NewAuditEntry(actor.email, actor.role, action, entityType, entityId, beforeJson, afterJson, actor.ip),
            )
        }.onFailure { log.warn("[audit] failed to record $action on $entityType/$entityId: ${it.message}") }
    }

    fun shelterCreated(actor: Actor, after: Shelter) =
        record(actor, AuditAction.SHELTER_CREATE, AuditEntity.SHELTER, after.id.toString(), afterJson = enc(after.toSnapshot()))

    fun shelterUpdated(actor: Actor, before: Shelter, after: Shelter) =
        record(actor, AuditAction.SHELTER_UPDATE, AuditEntity.SHELTER, after.id.toString(), enc(before.toSnapshot()), enc(after.toSnapshot()))

    fun shelterDeleted(actor: Actor, before: Shelter) =
        record(actor, AuditAction.SHELTER_DELETE, AuditEntity.SHELTER, before.id.toString(), beforeJson = enc(before.toSnapshot()))

    fun boardApproved(actor: Actor, before: ResourcePost) =
        record(actor, AuditAction.BOARD_APPROVE, AuditEntity.BOARD_POST, before.id.toString(), enc(before.toSnapshot()))

    fun boardRejected(actor: Actor, before: ResourcePost) =
        record(actor, AuditAction.BOARD_REJECT, AuditEntity.BOARD_POST, before.id.toString(), enc(before.toSnapshot()))

    fun sosHandled(actor: Actor, before: SosReport) =
        record(actor, AuditAction.SOS_HANDLE, AuditEntity.SOS, before.id.toString(), enc(before.toSnapshot()))

    fun sosReopened(actor: Actor, before: SosReport) =
        record(actor, AuditAction.SOS_REOPEN, AuditEntity.SOS, before.id.toString(), enc(before.toSnapshot()))

    fun sosDeleted(actor: Actor, before: SosReport) =
        record(actor, AuditAction.SOS_DELETE, AuditEntity.SOS, before.id.toString(), beforeJson = enc(before.toSnapshot()))

    fun searchDeleted(actor: Actor, before: SearchReport) =
        record(actor, AuditAction.SEARCH_DELETE, AuditEntity.SEARCH, before.id.toString(), beforeJson = enc(before.toSnapshot()))

    fun adminEvent(actor: Actor, action: String, adminId: Int?, note: String? = null) =
        record(actor, action, AuditEntity.ADMIN, adminId?.toString(), afterJson = note)

    fun login(email: String, role: String, ip: String?, success: Boolean) =
        record(Actor(email, role, ip), if (success) AuditAction.LOGIN_SUCCESS else AuditAction.LOGIN_FAILURE, AuditEntity.SESSION, null)

    private inline fun <reified T> enc(value: T): String = json.encodeToString(value)

    // ---- reading ---------------------------------------------------------------------------------

    fun list(filter: AuditFilter): List<AuditEntry> = audit.list(filter)

    /** Per-moderator activity, unioning admin_users with any actor seen only in the audit log. */
    fun moderators(): List<ModeratorActivity> {
        val counts = audit.actorActionCounts()
        val byActor = counts.groupBy { it.actorEmail }
        val admins = adminUsers.listAll()
        val known = admins.map { it.email }.toSet()
        val extraActors = byActor.keys.filterNot { it in known }
        return admins.map { u ->
            val rows = byActor[u.email].orEmpty()
            ModeratorActivity(
                email = u.email,
                role = u.role,
                enabled = u.enabled,
                counts = rows.associate { it.action to it.count },
                lastActiveAt = rows.mapNotNull { it.lastActiveAt }.maxOrNull(),
            )
        } + extraActors.map { email ->
            val rows = byActor[email].orEmpty()
            ModeratorActivity(email, role = "(removed)", enabled = false,
                counts = rows.associate { it.action to it.count },
                lastActiveAt = rows.mapNotNull { it.lastActiveAt }.maxOrNull())
        }
    }

    // ---- revert ----------------------------------------------------------------------------------

    fun revert(entryId: Int, actor: Actor): RevertOutcome {
        val e = audit.findById(entryId) ?: return RevertOutcome.NOT_FOUND
        if (e.revertedAt != null) return RevertOutcome.ALREADY_REVERTED
        if (e.action !in AuditAction.REVERTIBLE) return RevertOutcome.NOT_REVERTIBLE
        val ok = runCatching { applyInverse(e, actor) }.getOrElse {
            log.warn("[audit] revert of ${e.id} (${e.action}) failed: ${it.message}")
            false
        }
        if (!ok) return RevertOutcome.FAILED
        audit.markReverted(e.id, actor.email)
        record(actor, AuditAction.REVERT, e.entityType, e.entityId, afterJson = "reverted #${e.id} (${e.action})")
        return RevertOutcome.REVERTED
    }

    fun revertAllByModerator(email: String, actor: Actor): RevertAllResult {
        val entries = audit.listRevertibleByActor(email) // newest-first, non-reverted
        var reverted = 0
        val skipped = mutableListOf<RevertSkip>()
        for (e in entries) {
            val eid = e.entityId
            val latest = eid?.let { audit.latestForEntity(e.entityType, it) }
            // Conflict: a DIFFERENT moderator touched the same entity after this change.
            if (latest != null && latest.id != e.id && latest.createdAt.isAfter(e.createdAt) && latest.actorEmail != email) {
                skipped.add(RevertSkip(e.id, "changed later by ${latest.actorEmail}"))
                continue
            }
            val ok = runCatching { applyInverse(e, actor) }.getOrElse { false }
            if (ok) {
                audit.markReverted(e.id, actor.email)
                reverted++
            } else {
                skipped.add(RevertSkip(e.id, "revert failed"))
            }
        }
        adminEvent(actor, AuditAction.REVERT, null, "revert-all $email: reverted=$reverted skipped=${skipped.size}")
        return RevertAllResult(reverted, skipped)
    }

    /** Apply the inverse of a recorded action. Returns whether a row was affected. */
    private fun applyInverse(e: AuditEntry, actor: Actor): Boolean {
        val id = e.entityId?.toIntOrNull() ?: return false
        return when (e.action) {
            AuditAction.SHELTER_CREATE -> shelters.deactivate(id)
            AuditAction.SHELTER_UPDATE -> {
                val b = dec<ShelterSnapshot>(e.beforeJson) ?: return false
                shelters.update(b.id, b.toNewShelter()) != null
            }
            AuditAction.SHELTER_DELETE -> shelters.reactivate(id)
            AuditAction.BOARD_APPROVE -> board.setStatus(id, "PENDING")
            AuditAction.BOARD_REJECT -> {
                val b = dec<BoardPostSnapshot>(e.beforeJson) ?: return false
                board.restore(b.id, b.status, b.contactPhone, b.contactEmail, b.contactName, b.ownerSecret)
            }
            AuditAction.SOS_HANDLE -> sos.reopen(id)
            AuditAction.SOS_REOPEN -> sos.markHandled(id, actor.email)
            AuditAction.SOS_DELETE -> {
                val b = dec<SosSnapshot>(e.beforeJson) ?: return false
                sos.create(b.toNewSos()).id > 0
            }
            AuditAction.SEARCH_DELETE -> search.reopen(id)
            else -> false
        }
    }

    private inline fun <reified T> dec(s: String?): T? = s?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() }
}
