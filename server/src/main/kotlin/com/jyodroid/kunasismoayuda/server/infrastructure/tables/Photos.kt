package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Exposed mapping for `photos` (schema owned by Flyway V12). Images stored as bytea. */
object Photos : Table("photos") {
    val id = integer("id").autoIncrement()
    val contentType = varchar("content_type", 40)
    val data = binary("data")
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
