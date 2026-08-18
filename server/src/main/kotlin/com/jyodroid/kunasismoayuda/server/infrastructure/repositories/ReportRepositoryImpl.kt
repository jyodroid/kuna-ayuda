package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.DisasterReport
import com.jyodroid.kunasismoayuda.server.domain.repositories.ReportRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.DisasterReports
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class ReportRepositoryImpl : ReportRepository {

    override fun upsert(items: List<DisasterReport>): Int {
        if (!DatabaseFactory.initialized || items.isEmpty()) return 0
        return transaction {
            var touched = 0
            items.forEach { r ->
                val now = Instant.now()
                val updated = DisasterReports.update(
                    { (DisasterReports.sourceName eq r.source) and (DisasterReports.externalId eq r.externalId) },
                ) { it.applyReport(r, now) }
                if (updated == 0) {
                    DisasterReports.insert {
                        it[sourceName] = r.source
                        it[externalId] = r.externalId
                        it.applyReport(r, now)
                    }
                }
                touched++
            }
            touched
        }
    }

    override fun listRecent(limit: Int): List<DisasterReport> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            DisasterReports.selectAll()
                .orderBy(DisasterReports.publishedAt to SortOrder.DESC_NULLS_LAST)
                .limit(limit)
                .map { it.toReport() }
        }
    }

    private fun UpdateBuilder<*>.applyReport(r: DisasterReport, now: Instant) {
        this[DisasterReports.title] = r.title
        this[DisasterReports.body] = r.body
        this[DisasterReports.orgSource] = r.orgSource
        this[DisasterReports.country] = r.country
        this[DisasterReports.disasterType] = r.disasterType
        this[DisasterReports.url] = r.url
        this[DisasterReports.publishedAt] = r.publishedAt
        this[DisasterReports.fetchedAt] = now
    }

    private fun ResultRow.toReport() = DisasterReport(
        id = this[DisasterReports.id],
        source = this[DisasterReports.sourceName],
        externalId = this[DisasterReports.externalId],
        title = this[DisasterReports.title],
        body = this[DisasterReports.body],
        orgSource = this[DisasterReports.orgSource],
        country = this[DisasterReports.country],
        disasterType = this[DisasterReports.disasterType],
        url = this[DisasterReports.url],
        publishedAt = this[DisasterReports.publishedAt],
        fetchedAt = this[DisasterReports.fetchedAt],
    )
}
