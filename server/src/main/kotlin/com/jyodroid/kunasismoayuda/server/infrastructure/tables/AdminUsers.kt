package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Exposed mapping for `admin_users` (schema owned by Flyway V5__admin_users.sql). */
object AdminUsers : Table("admin_users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 160).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val role = varchar("role", 20)
    val enabled = bool("enabled").default(true)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
