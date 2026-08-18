package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Exposed mapping for `resource_posts` (schema owned by Flyway V2__resource_board.sql). */
object ResourcePosts : Table("resource_posts") {
    val id = integer("id").autoIncrement()
    val kind = varchar("kind", 20)
    val resourceType = varchar("resource_type", 30)
    val region = varchar("region", 120)
    val description = text("description")
    val contactPhone = varchar("contact_phone", 40).nullable()
    val contactEmail = varchar("contact_email", 160).nullable()
    val contactName = varchar("contact_name", 120).nullable()
    val status = varchar("status", 20)
    val ownerSecret = varchar("owner_secret", 64).nullable() // device-local ownership token (#4)
    val origin = varchar("source", 20) // property named `origin` to avoid clashing with Table.source
    val rawText = text("raw_text").nullable()
    val factCheck = text("fact_check").nullable() // moderator-facing Google Fact Check note (V17)
    val country = varchar("country", 2)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
