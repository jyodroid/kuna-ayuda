package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.repositories.ClassifyCacheEntry
import com.jyodroid.kunasismoayuda.server.domain.repositories.ClassifyCacheRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.ClassifyCache
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class ClassifyCacheRepositoryImpl : ClassifyCacheRepository {

    override fun get(contentHash: String): ClassifyCacheEntry? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            ClassifyCache.selectAll().where { ClassifyCache.contentHash eq contentHash }.singleOrNull()?.let {
                ClassifyCacheEntry(
                    kind = it[ClassifyCache.kind],
                    resourceType = it[ClassifyCache.resourceType],
                    region = it[ClassifyCache.region],
                    description = it[ClassifyCache.description],
                    contactPhone = it[ClassifyCache.contactPhone],
                    contactName = it[ClassifyCache.contactName],
                    factCheck = it[ClassifyCache.factCheck],
                    checked = it[ClassifyCache.checked],
                )
            }
        }
    }

    override fun put(contentHash: String, entry: ClassifyCacheEntry) {
        if (!DatabaseFactory.initialized) return
        transaction {
            // insertIgnore: a concurrent duplicate paste just no-ops rather than erroring on the PK.
            ClassifyCache.insertIgnore {
                it[ClassifyCache.contentHash] = contentHash
                it[kind] = entry.kind
                it[resourceType] = entry.resourceType
                it[region] = entry.region
                it[description] = entry.description
                it[contactPhone] = entry.contactPhone
                it[contactName] = entry.contactName
                it[factCheck] = entry.factCheck
                it[checked] = entry.checked
                it[createdAt] = LocalDateTime.now()
            }
        }
    }
}
