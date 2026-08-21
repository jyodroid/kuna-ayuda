package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

/** Exposed mapping for `classify_cache` (schema owned by Flyway V17). Memoized classify+factcheck result. */
object ClassifyCache : Table("classify_cache") {
    val contentHash = varchar("content_hash", 64)
    val kind = varchar("kind", 20)
    val resourceType = varchar("resource_type", 30)
    val region = varchar("region", 120)
    val description = text("description")
    val contactPhone = varchar("contact_phone", 40).nullable()
    val contactName = varchar("contact_name", 120).nullable()
    val factCheck = text("fact_check").nullable()
    val checked = bool("checked")
    val collectionPoints = text("collection_points").nullable() // JSON array of {name,address,hours}
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(contentHash)
}
