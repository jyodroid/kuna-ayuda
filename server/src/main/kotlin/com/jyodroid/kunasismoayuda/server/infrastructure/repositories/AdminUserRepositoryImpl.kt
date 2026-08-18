package com.jyodroid.kunasismoayuda.server.infrastructure.repositories

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.domain.models.AdminUser
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.infrastructure.tables.AdminUsers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class AdminUserRepositoryImpl : AdminUserRepository {

    override fun findByEmail(email: String): AdminUser? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            AdminUsers.selectAll().where { AdminUsers.email eq email }
                .singleOrNull()?.toAdminUser()
        }
    }

    override fun findById(id: Int): AdminUser? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            AdminUsers.selectAll().where { AdminUsers.id eq id }.singleOrNull()?.toAdminUser()
        }
    }

    override fun listAll(): List<AdminUser> {
        if (!DatabaseFactory.initialized) return emptyList()
        return transaction {
            AdminUsers.selectAll().orderBy(AdminUsers.email).map { it.toAdminUser() }
        }
    }

    override fun create(email: String, passwordHash: String, role: String): AdminUser? {
        if (!DatabaseFactory.initialized) return null
        return transaction {
            val exists = AdminUsers.selectAll().where { AdminUsers.email eq email }.any()
            if (exists) return@transaction null
            val id = AdminUsers.insert {
                it[AdminUsers.email] = email
                it[AdminUsers.passwordHash] = passwordHash
                it[AdminUsers.role] = role
                it[createdAt] = LocalDateTime.now()
            } get AdminUsers.id
            AdminUsers.selectAll().where { AdminUsers.id eq id }.single().toAdminUser()
        }
    }

    override fun deleteById(id: Int): Boolean {
        if (!DatabaseFactory.initialized) return false
        return transaction {
            AdminUsers.deleteWhere { AdminUsers.id eq id } > 0
        }
    }

    override fun createIfAbsent(email: String, passwordHash: String, role: String): Boolean {
        if (!DatabaseFactory.initialized) return false
        return transaction {
            val exists = AdminUsers.selectAll().where { AdminUsers.email eq email }.any()
            if (exists) return@transaction false
            AdminUsers.insert {
                it[AdminUsers.email] = email
                it[AdminUsers.passwordHash] = passwordHash
                it[AdminUsers.role] = role
                it[createdAt] = LocalDateTime.now()
            }
            true
        }
    }

    private fun ResultRow.toAdminUser() = AdminUser(
        id = this[AdminUsers.id],
        email = this[AdminUsers.email],
        passwordHash = this[AdminUsers.passwordHash],
        role = this[AdminUsers.role],
    )
}
