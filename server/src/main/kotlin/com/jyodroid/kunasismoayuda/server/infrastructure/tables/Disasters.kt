package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/** Exposed mapping for `disasters` (schema owned by Flyway migration V9__disasters_reports.sql). */
object Disasters : Table("disasters") {
    val id = integer("id").autoIncrement()
    val sourceName = varchar("source", 20)
    val externalId = varchar("external_id", 64)
    val eventType = varchar("event_type", 20)
    val title = varchar("title", 300)
    val description = text("description").nullable()
    val country = varchar("country", 120).nullable()
    val iso3 = varchar("iso3", 3).nullable()
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val magnitude = double("magnitude").nullable()
    val alertLevel = varchar("alert_level", 20).nullable()
    val severityText = varchar("severity_text", 200).nullable()
    val eventDate = timestamp("event_date").nullable()
    val url = varchar("url", 500).nullable()
    val fetchedAt = timestamp("fetched_at")

    override val primaryKey = PrimaryKey(id)
}
