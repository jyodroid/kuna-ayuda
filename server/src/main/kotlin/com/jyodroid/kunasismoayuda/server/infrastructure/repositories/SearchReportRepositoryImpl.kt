package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.NewSearchReport
import com.jyodroid.kunasismoayuda.server.domain.models.SearchReport
import com.jyodroid.kunasismoayuda.server.domain.repositories.SearchRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.Photos
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.SearchReports
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class SearchReportRepositoryImpl : SearchRepository {

    override fun listActive(subject: String?, state: String?, country: String): List<SearchReport> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            val query = SearchReports.selectAll().where { SearchReports.status eq "ACTIVE" }
            query.andWhere { SearchReports.country eq country.uppercase() }
            subject?.let { query.andWhere { SearchReports.subject eq it } }
            state?.let { query.andWhere { SearchReports.state eq it } }
            query.orderBy(SearchReports.createdAt, SortOrder.DESC).map { it.toReport() }
        }
    }

    override fun create(report: NewSearchReport): SearchReport = transaction {
        val id = SearchReports.insert {
            it[subject] = report.subject
            it[state] = report.state
            it[title] = report.title
            it[description] = report.description
            it[lastSeen] = report.lastSeen
            it[notes] = report.notes
            it[contactPhone] = report.contactPhone
            it[contactName] = report.contactName
            it[photoId] = report.photoId
            it[country] = report.country.uppercase()
            it[status] = report.status
            it[createdAt] = LocalDateTime.now()
        } get SearchReports.id

        SearchReports.selectAll().where { SearchReports.id eq id }.single().toReport()
    }

    override fun close(id: Int): Boolean = transaction {
        SearchReports.update({ SearchReports.id eq id }) { it[status] = "CLOSED" } > 0
    }

    override fun find(id: Int): SearchReport? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            SearchReports.selectAll().where { SearchReports.id eq id }.singleOrNull()?.toReport()
        }
    }

    override fun reopen(id: Int): Boolean = transaction {
        SearchReports.update({ SearchReports.id eq id }) { it[status] = "ACTIVE" } > 0
    }

    override fun expireOlderThan(cutoff: LocalDateTime): Int {
        if (!DatabaseFactory.initialized) return 0
        return transaction {
            SearchReports.update({
                (SearchReports.status eq "ACTIVE") and (SearchReports.createdAt less cutoff)
            }) {
                it[status] = "CLOSED"
            }
        }
    }

    override fun deleteOlderThan(cutoff: LocalDateTime): Int {
        if (!DatabaseFactory.initialized) return 0
        return transaction {
            // Capture the reports' photos first (FK is SET NULL, not cascade), delete the reports,
            // then delete those photo rows so the bytea doesn't leak.
            val photoIds = SearchReports.selectAll()
                .where { SearchReports.createdAt less cutoff }
                .mapNotNull { it[SearchReports.photoId] }
            val deleted = SearchReports.deleteWhere { SearchReports.createdAt less cutoff }
            if (photoIds.isNotEmpty()) {
                Photos.deleteWhere { Photos.id inList photoIds }
            }
            deleted
        }
    }

    private fun ResultRow.toReport() = SearchReport(
        id = this[SearchReports.id],
        subject = this[SearchReports.subject],
        state = this[SearchReports.state],
        title = this[SearchReports.title],
        description = this[SearchReports.description],
        lastSeen = this[SearchReports.lastSeen],
        notes = this[SearchReports.notes],
        contactPhone = this[SearchReports.contactPhone],
        contactName = this[SearchReports.contactName],
        photoId = this[SearchReports.photoId],
        country = this[SearchReports.country],
        status = this[SearchReports.status],
        createdAt = this[SearchReports.createdAt],
    )
}
