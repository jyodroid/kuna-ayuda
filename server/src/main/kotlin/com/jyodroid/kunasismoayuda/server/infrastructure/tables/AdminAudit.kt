package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Exposed mapping for `admin_audit` (schema owned by Flyway V19__admin_audit_and_accounts.sql). */
object AdminAudit : Table("admin_audit") {
    val id = integer("id").autoIncrement()
    val actorEmail = varchar("actor_email", 160)
    val actorRole = varchar("actor_role", 20)
    val action = varchar("action", 40)
    val entityType = varchar("entity_type", 40)
    val entityId = varchar("entity_id", 64).nullable()
    val beforeJson = text("before_json").nullable()
    val afterJson = text("after_json").nullable()
    val ip = varchar("ip", 64).nullable()
    val createdAt = datetime("created_at")
    val revertedAt = datetime("reverted_at").nullable()
    val revertedBy = varchar("reverted_by", 160).nullable()

    override val primaryKey = PrimaryKey(id)
}
