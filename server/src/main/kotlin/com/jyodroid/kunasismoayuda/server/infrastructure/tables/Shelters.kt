package com.jyodroid.kunasismoayuda.server.infrastructure.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

/** Exposed mapping for the `shelters` table (schema owned by Flyway migration V1__init.sql). */
object Shelters : Table("shelters") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 200)
    val type = varchar("type", 40)
    val address = varchar("address", 300)
    val latitude = double("latitude")
    val longitude = double("longitude")
    val accepts = text("accepts")
    val hours = varchar("hours", 200).nullable()
    val contactPhone = varchar("contact_phone", 40).nullable()
    val verified = bool("verified")
    val lastVerified = date("last_verified").nullable()
    val active = bool("active")
    val country = varchar("country", 2)

    override val primaryKey = PrimaryKey(id)
}
