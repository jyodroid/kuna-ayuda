package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Exposed mapping for `search_reports` (Lost & Found, schema owned by Flyway V12). */
object SearchReports : Table("search_reports") {
    val id = integer("id").autoIncrement()
    val subject = varchar("subject", 10)     // PET | PERSON
    val state = varchar("state", 10)         // LOST | FOUND
    val title = varchar("title", 160)
    val description = text("description")
    val lastSeen = varchar("last_seen", 160)
    val notes = text("notes").nullable()
    val contactPhone = varchar("contact_phone", 40)
    val contactName = varchar("contact_name", 120).nullable()
    val photoId = integer("photo_id").references(Photos.id).nullable()
    val country = varchar("country", 2)
    val status = varchar("status", 20)       // ACTIVE | CLOSED
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
