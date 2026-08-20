package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.Actor
import com.jyodroid.kunasismoayuda.server.domain.models.AuditAction
import com.jyodroid.kunasismoayuda.server.domain.models.AuditEntity
import com.jyodroid.kunasismoayuda.server.domain.models.AuditEntry
import com.jyodroid.kunasismoayuda.server.domain.models.AuditFilter
import com.jyodroid.kunasismoayuda.server.domain.models.NewAuditEntry
import com.jyodroid.kunasismoayuda.server.domain.models.ResourcePost
import com.jyodroid.kunasismoayuda.server.domain.models.SearchReport
import com.jyodroid.kunasismoayuda.server.domain.models.Shelter
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import com.jyodroid.kunasismoayuda.server.domain.repositories.ActorActionCount
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.AuditRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.ShelterRepository
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AuditServiceTest {

    private val actor = Actor("owner@kuna.org", "SUPERADMIN", "1.2.3.4")

    /** Minimal in-memory audit repo with auto-increment ids and reverted stamping. */
    private class FakeAuditRepo : AuditRepository {
        val entries = mutableListOf<AuditEntry>()
        private var nextId = 1
        override fun record(entry: NewAuditEntry): Int {
            val id = nextId++
            entries.add(
                AuditEntry(id, entry.actorEmail, entry.actorRole, entry.action, entry.entityType,
                    entry.entityId, entry.beforeJson, entry.afterJson, entry.ip, LocalDateTime.now(), null, null),
            )
            return id
        }
        override fun findById(id: Int) = entries.firstOrNull { it.id == id }
        override fun list(filter: AuditFilter) = entries.sortedByDescending { it.createdAt }
        override fun listRevertibleByActor(email: String) =
            entries.filter { it.actorEmail == email && it.revertedAt == null && it.action in AuditAction.REVERTIBLE }
                .sortedByDescending { it.createdAt }
        override fun latestForEntity(entityType: String, entityId: String) =
            entries.filter { it.entityType == entityType && it.entityId == entityId }.maxByOrNull { it.createdAt }
        override fun markReverted(id: Int, by: String): Boolean {
            val i = entries.indexOfFirst { it.id == id && it.revertedAt == null }
            if (i < 0) return false
            entries[i] = entries[i].copy(revertedAt = LocalDateTime.now(), revertedBy = by)
            return true
        }
        override fun actorActionCounts(): List<ActorActionCount> =
            entries.groupBy { it.actorEmail to it.action }.map { (k, v) ->
                ActorActionCount(k.first, k.second, v.size.toLong(), v.maxOf { it.createdAt }.toString())
            }
    }

    private fun shelter(id: Int, name: String) = Shelter(
        id, name, "ACOPIO", "Calle 1, Bogotá", 4.6, -74.1, "agua", null, null, true, null, true, "CO",
    )

    private fun services(): Triple<AuditService, FakeAuditRepo, Repos> {
        val audit = FakeAuditRepo()
        val repos = Repos(
            shelters = mockk(relaxed = true),
            board = mockk(relaxed = true),
            sos = mockk(relaxed = true),
            search = mockk(relaxed = true),
            admins = mockk(relaxed = true),
        )
        // Make the inverse operations "succeed" (relaxed mocks return false/null by default).
        every { repos.shelters.update(any(), any()) } returns shelter(1, "restored")
        every { repos.shelters.deactivate(any()) } returns true
        every { repos.shelters.reactivate(any()) } returns true
        every { repos.board.setStatus(any(), any()) } returns true
        every { repos.board.restore(any(), any(), any(), any(), any(), any()) } returns true
        every { repos.sos.reopen(any()) } returns true
        every { repos.sos.markHandled(any(), any()) } returns true
        every { repos.search.reopen(any()) } returns true
        val service = AuditService(audit, repos.admins, repos.shelters, repos.board, repos.sos, repos.search)
        return Triple(service, audit, repos)
    }

    private class Repos(
        val shelters: ShelterRepository,
        val board: ResourceBoardRepository,
        val sos: SosRepository,
        val search: SearchRepository,
        val admins: AdminUserRepository,
    )

    @Test
    fun `shelter update is recorded then reverted to the before snapshot`() {
        val (service, audit, repos) = services()
        val before = shelter(7, "Old name")
        val after = shelter(7, "New name")
        service.shelterUpdated(actor, before, after)

        val entry = audit.entries.single()
        assertEquals(AuditAction.SHELTER_UPDATE, entry.action)
        assertEquals("7", entry.entityId)

        val outcome = service.revert(entry.id, actor)
        assertEquals(RevertOutcome.REVERTED, outcome)
        // The inverse restores the BEFORE name.
        val slot = slot<com.jyodroid.kunasismoayuda.server.domain.models.NewShelter>()
        verify { repos.shelters.update(7, capture(slot)) }
        assertEquals("Old name", slot.captured.name)
        // The entry is now marked reverted, and a REVERT entry was appended.
        assertTrue(audit.findById(entry.id)!!.revertedAt != null)
        assertTrue(audit.entries.any { it.action == AuditAction.REVERT })
    }

    @Test
    fun `board approve reverts to pending`() {
        val (service, audit, repos) = services()
        val post = ResourcePost(3, "REQUEST", "WATER", "Bogotá", "need water", null, null, null,
            "PENDING", "manual", null, null, "CO", null, LocalDateTime.now())
        service.boardApproved(actor, post)
        val id = audit.entries.single().id
        assertEquals(RevertOutcome.REVERTED, service.revert(id, actor))
        verify { repos.board.setStatus(3, "PENDING") }
    }

    @Test
    fun `already reverted and non-revertible and missing entries are rejected`() {
        val (service, audit, _) = services()
        // missing
        assertEquals(RevertOutcome.NOT_FOUND, service.revert(999, actor))
        // non-revertible (a login event)
        service.login("mod@kuna.org", "ADMIN", "1.1.1.1", success = true)
        val loginId = audit.entries.single { it.action == AuditAction.LOGIN_SUCCESS }.id
        assertEquals(RevertOutcome.NOT_REVERTIBLE, service.revert(loginId, actor))
        // already reverted
        service.shelterDeleted(actor, shelter(5, "x"))
        val delId = audit.entries.single { it.action == AuditAction.SHELTER_DELETE }.id
        assertEquals(RevertOutcome.REVERTED, service.revert(delId, actor))
        assertEquals(RevertOutcome.ALREADY_REVERTED, service.revert(delId, actor))
    }

    @Test
    fun `revert-all skips an entity a different moderator touched later`() {
        val (service, audit, _) = services()
        val mod = Actor("mod@kuna.org", "ADMIN", "9.9.9.9")
        // mod deletes shelter 10 and shelter 11
        service.shelterDeleted(mod, shelter(10, "ten"))
        service.shelterDeleted(mod, shelter(11, "eleven"))
        // then a DIFFERENT moderator edits shelter 11 afterwards (conflict)
        service.shelterUpdated(actor, shelter(11, "eleven"), shelter(11, "eleven-edited"))

        val result = service.revertAllByModerator("mod@kuna.org", actor)
        assertEquals(1, result.reverted) // only shelter 10
        assertEquals(1, result.skipped.size) // shelter 11 skipped (conflict)
        assertTrue(result.skipped.single().reason.contains("owner@kuna.org"))
    }

    @Test
    fun `moderators summary aggregates counts per actor`() {
        val (service, _, repos) = services()
        every { repos.admins.listAll() } returns listOf(
            com.jyodroid.kunasismoayuda.server.domain.models.AdminUser(1, "mod@kuna.org", "hash", "ADMIN", true),
        )
        val mod = Actor("mod@kuna.org", "ADMIN", null)
        service.shelterCreated(mod, shelter(1, "a"))
        service.shelterCreated(mod, shelter(2, "b"))
        service.boardApproved(mod, ResourcePost(1, "OFFER", "FOOD", "x", "y", null, null, null,
            "PENDING", "manual", null, null, "CO", null, LocalDateTime.now()))

        val summary = service.moderators().single { it.email == "mod@kuna.org" }
        assertEquals(2L, summary.counts[AuditAction.SHELTER_CREATE])
        assertEquals(1L, summary.counts[AuditAction.BOARD_APPROVE])
        assertTrue(summary.enabled)
    }
}
