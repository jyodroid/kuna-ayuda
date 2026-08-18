package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.Photo
import com.jyodroid.kunasismoayuda.server.domain.repositories.PhotoRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.Photos
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class PhotoRepositoryImpl : PhotoRepository {

    override fun save(contentType: String, data: ByteArray): Int = transaction {
        Photos.insert {
            it[Photos.contentType] = contentType
            it[Photos.data] = data
            it[createdAt] = LocalDateTime.now()
        } get Photos.id
    }

    override fun find(id: Int): Photo? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            Photos.selectAll().where { Photos.id eq id }.singleOrNull()?.let {
                Photo(
                    id = it[Photos.id],
                    contentType = it[Photos.contentType],
                    data = it[Photos.data],
                )
            }
        }
    }
}
