package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.Disaster
import com.jyodroid.kunasismoayuda.server.domain.repositories.DisasterRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.Disasters
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

class DisasterRepositoryImpl : DisasterRepository {

    override fun upsert(items: List<Disaster>): Int {
        if (!DatabaseFactory.initialized || items.isEmpty()) return 0
        return transaction {
            var touched = 0
            items.forEach { d ->
                val now = Instant.now()
                val updated = Disasters.update(
                    { (Disasters.sourceName eq d.source) and (Disasters.externalId eq d.externalId) },
                ) { it.applyDisaster(d, now) }
                if (updated == 0) {
                    Disasters.insert {
                        it[sourceName] = d.source
                        it[externalId] = d.externalId
                        it.applyDisaster(d, now)
                    }
                }
                touched++
            }
            touched
        }
    }

    override fun listRecent(limit: Int): List<Disaster> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            Disasters.selectAll()
                .orderBy(Disasters.eventDate to SortOrder.DESC_NULLS_LAST)
                .limit(limit)
                .map { it.toDisaster() }
        }
    }

    /** Sets every mutable column (not the natural key) from [d]; used by both insert and update. */
    private fun org.jetbrains.exposed.sql.statements.UpdateBuilder<*>.applyDisaster(d: Disaster, now: Instant) {
        this[Disasters.eventType] = d.eventType
        this[Disasters.title] = d.title
        this[Disasters.description] = d.description
        this[Disasters.country] = d.country
        this[Disasters.iso3] = d.iso3
        this[Disasters.latitude] = d.latitude
        this[Disasters.longitude] = d.longitude
        this[Disasters.magnitude] = d.magnitude
        this[Disasters.alertLevel] = d.alertLevel
        this[Disasters.severityText] = d.severityText
        this[Disasters.eventDate] = d.eventDate
        this[Disasters.url] = d.url
        this[Disasters.fetchedAt] = now
    }

    private fun ResultRow.toDisaster() = Disaster(
        id = this[Disasters.id],
        source = this[Disasters.sourceName],
        externalId = this[Disasters.externalId],
        eventType = this[Disasters.eventType],
        title = this[Disasters.title],
        description = this[Disasters.description],
        country = this[Disasters.country],
        iso3 = this[Disasters.iso3],
        latitude = this[Disasters.latitude],
        longitude = this[Disasters.longitude],
        magnitude = this[Disasters.magnitude],
        alertLevel = this[Disasters.alertLevel],
        severityText = this[Disasters.severityText],
        eventDate = this[Disasters.eventDate],
        url = this[Disasters.url],
        fetchedAt = this[Disasters.fetchedAt],
    )
}
