package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.AuditAction
import com.jyodroid.kunasismoayuda.server.domain.models.AuditEntry
import com.jyodroid.kunasismoayuda.server.domain.models.AuditFilter
import com.jyodroid.kunasismoayuda.server.domain.models.NewAuditEntry
import com.jyodroid.kunasismoayuda.server.domain.repositories.ActorActionCount
import com.jyodroid.kunasismoayuda.server.domain.repositories.AuditRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.AdminAudit
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class AuditRepositoryImpl : AuditRepository {

    override fun record(entry: NewAuditEntry): Int? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            AdminAudit.insert {
                it[actorEmail] = entry.actorEmail
                it[actorRole] = entry.actorRole
                it[action] = entry.action
                it[entityType] = entry.entityType
                it[entityId] = entry.entityId
                it[beforeJson] = entry.beforeJson
                it[afterJson] = entry.afterJson
                it[ip] = entry.ip
                it[createdAt] = LocalDateTime.now()
            } get AdminAudit.id
        }
    }

    override fun findById(id: Int): AuditEntry? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            AdminAudit.selectAll().where { AdminAudit.id eq id }.singleOrNull()?.toEntry()
        }
    }

    override fun list(filter: AuditFilter): List<AuditEntry> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            val query = AdminAudit.selectAll()
            filter.actorEmail?.let { query.andWhere { AdminAudit.actorEmail eq it } }
            filter.action?.let { query.andWhere { AdminAudit.action eq it } }
            filter.entityType?.let { query.andWhere { AdminAudit.entityType eq it } }
            filter.reverted?.let { rev ->
                if (rev) query.andWhere { AdminAudit.revertedAt.isNotNull() }
                else query.andWhere { AdminAudit.revertedAt.isNull() }
            }
            query.orderBy(AdminAudit.createdAt, SortOrder.DESC)
                .limit(filter.limit.coerceIn(1, 500))
                .offset(filter.offset.coerceAtLeast(0).toLong())
                .map { it.toEntry() }
        }
    }

    override fun listRevertibleByActor(email: String): List<AuditEntry> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            AdminAudit.selectAll()
                .where { AdminAudit.actorEmail eq email }
                .andWhere { AdminAudit.revertedAt.isNull() }
                .andWhere { AdminAudit.action inList AuditAction.REVERTIBLE }
                .orderBy(AdminAudit.createdAt, SortOrder.DESC)
                .map { it.toEntry() }
        }
    }

    override fun latestForEntity(entityType: String, entityId: String): AuditEntry? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            AdminAudit.selectAll()
                .where { AdminAudit.entityType eq entityType }
                .andWhere { AdminAudit.entityId eq entityId }
                .orderBy(AdminAudit.createdAt, SortOrder.DESC)
                .limit(1)
                .firstOrNull()?.toEntry()
        }
    }

    override fun markReverted(id: Int, by: String): Boolean {
        if (!DatabaseFactory.initialized) return false
        return transaction {
            AdminAudit.update({ (AdminAudit.id eq id) and AdminAudit.revertedAt.isNull() }) {
                it[revertedAt] = LocalDateTime.now()
                it[revertedBy] = by
            } > 0
        }
    }

    override fun actorActionCounts(): List<ActorActionCount> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            val rows = AdminAudit.selectAll().map { it.toEntry() }
            val lastActive = rows.groupBy { it.actorEmail }
                .mapValues { (_, list) -> list.maxOf { it.createdAt } }
            rows.groupBy { it.actorEmail to it.action }
                .map { (key, list) ->
                    ActorActionCount(
                        actorEmail = key.first,
                        action = key.second,
                        count = list.size.toLong(),
                        lastActiveAt = lastActive[key.first]?.toString(),
                    )
                }
        }
    }

    private fun ResultRow.toEntry() = AuditEntry(
        id = this[AdminAudit.id],
        actorEmail = this[AdminAudit.actorEmail],
        actorRole = this[AdminAudit.actorRole],
        action = this[AdminAudit.action],
        entityType = this[AdminAudit.entityType],
        entityId = this[AdminAudit.entityId],
        beforeJson = this[AdminAudit.beforeJson],
        afterJson = this[AdminAudit.afterJson],
        ip = this[AdminAudit.ip],
        createdAt = this[AdminAudit.createdAt],
        revertedAt = this[AdminAudit.revertedAt],
        revertedBy = this[AdminAudit.revertedBy],
    )
}
