package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Exposed mapping for `sos_reports` (schema owned by Flyway V3__sos.sql). */
object SosReports : Table("sos_reports") {
    val id = integer("id").autoIncrement()
    val status = varchar("status", 10)
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val region = varchar("region", 120).nullable()
    val message = text("message").nullable()
    val contactPhone = varchar("contact_phone", 40).nullable()
    // ISO country code (V10, default 'CO'); scopes the public "safe" list per country.
    val country = varchar("country", 2)
    // Public name on an "I'm safe" check-in (V22); null for legacy/nameless rows and SOS reports.
    val displayName = varchar("display_name", 120).nullable()
    val createdAt = datetime("created_at")
    val handledAt = datetime("handled_at").nullable()   // null = pending; set = archived
    val handledBy = varchar("handled_by", 120).nullable()

    override val primaryKey = PrimaryKey(id)
}
