package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table

/** Exposed mapping for `api_usage` (schema owned by Flyway V17). Per-feature monthly call counter. */
object ApiUsage : Table("api_usage") {
    val feature = varchar("feature", 40)
    val period = varchar("period", 7) // 'YYYY-MM'
    val count = integer("count")

    override val primaryKey = PrimaryKey(feature, period)
}
