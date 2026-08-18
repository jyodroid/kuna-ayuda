package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.AdminAccount

/** Super-admin-only account management (`/api/admins`); calls carry the moderator bearer token. */
interface AdminRepository {
    suspend fun list(): List<AdminAccount>
    suspend fun create(email: String, password: String): AdminAccount
    suspend fun delete(id: Int)
}
