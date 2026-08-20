package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.NewSosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SosStats
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.SosReports
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class SosRepositoryImpl : SosRepository {

    override fun create(report: NewSosReport): SosReport = transaction {
        val id = SosReports.insert {
            it[status] = report.status
            it[latitude] = report.latitude
            it[longitude] = report.longitude
            it[region] = report.region
            it[message] = report.message
            it[contactPhone] = report.contactPhone
            it[createdAt] = LocalDateTime.now()
        } get SosReports.id

        SosReports.selectAll().where { SosReports.id eq id }.single().toReport()
    }

    override fun find(id: Int): SosReport? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            SosReports.selectAll().where { SosReports.id eq id }.singleOrNull()?.toReport()
        }
    }

    override fun list(status: String?, archived: Boolean?): List<SosReport> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            val query = SosReports.selectAll()
            if (status != null) query.andWhere { SosReports.status eq status }
            when (archived) {
                false -> query.andWhere { SosReports.handledAt.isNull() }
                true -> query.andWhere { SosReports.handledAt.isNotNull() }
                null -> Unit // both
            }
            query.orderBy(SosReports.createdAt, SortOrder.DESC).map { it.toReport() }
        }
    }

    override fun markHandled(id: Int, by: String?): Boolean {
        if (!DatabaseFactory.initialized) return false
        return transaction {
            SosReports.update({ SosReports.id eq id }) {
                it[handledAt] = LocalDateTime.now()
                it[handledBy] = by
            } > 0
        }
    }

    override fun reopen(id: Int): Boolean {
        if (!DatabaseFactory.initialized) return false
        return transaction {
            SosReports.update({ SosReports.id eq id }) {
                it[handledAt] = null
                it[handledBy] = null
            } > 0
        }
    }

    override fun delete(id: Int): Boolean {
        if (!DatabaseFactory.initialized) return false
        return transaction {
            SosReports.deleteWhere { SosReports.id eq id } > 0
        }
    }

    override fun stats(): SosStats {
        if (!DatabaseFactory.initialized) return SosStats(0, 0, 0, 0)
        return transaction {
            fun count(status: String, handled: Boolean): Int =
                SosReports.selectAll()
                    .where { SosReports.status eq status }
                    .andWhere { if (handled) SosReports.handledAt.isNotNull() else SosReports.handledAt.isNull() }
                    .count().toInt()
            SosStats(
                pendingSos = count("SOS", handled = false),
                pendingSafe = count("SAFE", handled = false),
                handledSos = count("SOS", handled = true),
                handledSafe = count("SAFE", handled = true),
            )
        }
    }

    private fun ResultRow.toReport() = SosReport(
        id = this[SosReports.id],
        status = this[SosReports.status],
        latitude = this[SosReports.latitude],
        longitude = this[SosReports.longitude],
        region = this[SosReports.region],
        message = this[SosReports.message],
        contactPhone = this[SosReports.contactPhone],
        createdAt = this[SosReports.createdAt],
        handledAt = this[SosReports.handledAt],
        handledBy = this[SosReports.handledBy],
    )
}
