package com.jyodroid.kunasismoayuda.server.domain.repositories

import com.jyodroid.kunasismoayuda.server.domain.models.AdminUser

interface AdminUserRepository {
    fun findByEmail(email: String): AdminUser?
    fun findById(id: Int): AdminUser?
    fun listAll(): List<AdminUser>

    /** Insert a moderator if one with this email doesn't already exist. Returns true if created. */
    fun createIfAbsent(email: String, passwordHash: String, role: String = "ADMIN"): Boolean

    /** Create an admin; returns the new row, or null if the email is already taken. */
    fun create(email: String, passwordHash: String, role: String): AdminUser?

    /** Delete by id. Returns true if a row was removed. */
    fun deleteById(id: Int): Boolean
}
