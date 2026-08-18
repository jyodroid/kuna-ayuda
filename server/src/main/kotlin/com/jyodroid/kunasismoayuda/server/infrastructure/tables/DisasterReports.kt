package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/** Exposed mapping for `disaster_reports` (schema owned by Flyway migration V9__disasters_reports.sql). */
object DisasterReports : Table("disaster_reports") {
    val id = integer("id").autoIncrement()
    val sourceName = varchar("source", 20)
    val externalId = varchar("external_id", 64)
    val title = varchar("title", 500)
    val body = text("body").nullable()
    val orgSource = varchar("org_source", 200).nullable()
    val country = varchar("country", 120).nullable()
    val disasterType = varchar("disaster_type", 80).nullable()
    val url = varchar("url", 500).nullable()
    val publishedAt = timestamp("published_at").nullable()
    val fetchedAt = timestamp("fetched_at")

    override val primaryKey = PrimaryKey(id)
}
