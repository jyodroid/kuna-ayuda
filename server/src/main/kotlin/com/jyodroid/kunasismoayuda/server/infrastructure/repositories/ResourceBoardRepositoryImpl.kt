package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.NewResourcePost
import com.jyodroid.kunasismoayuda.server.domain.models.ResourcePost
import com.jyodroid.kunasismoayuda.server.domain.repositories.ResourceBoardRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.ResourcePosts
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class ResourceBoardRepositoryImpl : ResourceBoardRepository {

    override fun listActive(kind: String?, region: String?, resourceType: String?, country: String): List<ResourcePost> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            val query = ResourcePosts.selectAll().where { ResourcePosts.status eq "ACTIVE" }
            query.andWhere { ResourcePosts.country eq country.uppercase() }
            kind?.let { query.andWhere { ResourcePosts.kind eq it } }
            region?.let { query.andWhere { ResourcePosts.region eq it } }
            resourceType?.let { query.andWhere { ResourcePosts.resourceType eq it } }
            query.orderBy(ResourcePosts.createdAt, SortOrder.DESC).map { it.toPost() }
        }
    }

    override fun listByStatus(status: String): List<ResourcePost> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            ResourcePosts.selectAll().where { ResourcePosts.status eq status }
                .orderBy(ResourcePosts.createdAt, SortOrder.DESC)
                .map { it.toPost() }
        }
    }

    override fun create(post: NewResourcePost): ResourcePost = transaction {
        val id = ResourcePosts.insert {
            it[kind] = post.kind
            it[resourceType] = post.resourceType
            it[region] = post.region
            it[description] = post.description
            it[contactPhone] = post.contactPhone
            it[contactEmail] = post.contactEmail
            it[contactName] = post.contactName
            it[status] = post.status
            it[ownerSecret] = post.ownerSecret
            it[origin] = post.source
            it[rawText] = post.rawText
            it[factCheck] = post.factCheck
            it[country] = post.country.uppercase()
            it[createdAt] = LocalDateTime.now()
        } get ResourcePosts.id

        ResourcePosts.selectAll().where { ResourcePosts.id eq id }.single().toPost()
    }

    override fun find(id: Int): ResourcePost? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            ResourcePosts.selectAll().where { ResourcePosts.id eq id }.singleOrNull()?.toPost()
        }
    }

    override fun setStatus(id: Int, status: String): Boolean = transaction {
        ResourcePosts.update({ ResourcePosts.id eq id }) { it[ResourcePosts.status] = status } > 0
    }

    override fun close(id: Int): Boolean = transaction {
        // Closing a post scrubs its contact info: once it's down, the poster's phone/email/name are
        // deleted (privacy — contact is public only while the post is live). See #3.
        ResourcePosts.update({ ResourcePosts.id eq id }) {
            it[status] = "CLOSED"
            it[contactPhone] = null
            it[contactEmail] = null
            it[contactName] = null
            it[ownerSecret] = null // the ownership token is spent once the post is down
        } > 0
    }

    override fun restore(
        id: Int,
        status: String,
        contactPhone: String?,
        contactEmail: String?,
        contactName: String?,
        ownerSecret: String?,
    ): Boolean = transaction {
        ResourcePosts.update({ ResourcePosts.id eq id }) {
            it[ResourcePosts.status] = status
            it[ResourcePosts.contactPhone] = contactPhone
            it[ResourcePosts.contactEmail] = contactEmail
            it[ResourcePosts.contactName] = contactName
            it[ResourcePosts.ownerSecret] = ownerSecret
        } > 0
    }

    override fun expireOlderThan(cutoff: LocalDateTime): Int {
        if (!DatabaseFactory.initialized) return 0
        // Bulk version of close(): closes stale ACTIVE posts and scrubs their contact + owner token.
        return transaction {
            ResourcePosts.update({
                (ResourcePosts.status eq "ACTIVE") and (ResourcePosts.createdAt less cutoff)
            }) {
                it[status] = "CLOSED"
                it[contactPhone] = null
                it[contactEmail] = null
                it[contactName] = null
                it[ownerSecret] = null
            }
        }
    }

    private fun ResultRow.toPost() = ResourcePost(
        id = this[ResourcePosts.id],
        kind = this[ResourcePosts.kind],
        resourceType = this[ResourcePosts.resourceType],
        region = this[ResourcePosts.region],
        description = this[ResourcePosts.description],
        contactPhone = this[ResourcePosts.contactPhone],
        contactEmail = this[ResourcePosts.contactEmail],
        contactName = this[ResourcePosts.contactName],
        status = this[ResourcePosts.status],
        source = this[ResourcePosts.origin],
        rawText = this[ResourcePosts.rawText],
        factCheck = this[ResourcePosts.factCheck],
        country = this[ResourcePosts.country],
        ownerSecret = this[ResourcePosts.ownerSecret],
        createdAt = this[ResourcePosts.createdAt],
    )
}
