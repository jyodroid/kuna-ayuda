package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.auth.SessionManager
import com.jyodroid.kunasismoayuda.core.data.remote.AdminApi
import com.jyodroid.kunasismoayuda.core.data.remote.AdminDto
import com.jyodroid.kunasismoayuda.core.domain.model.AdminAccount
import com.jyodroid.kunasismoayuda.core.domain.repository.AdminRepository

class AdminRepositoryImpl(
    private val api: AdminApi,
    private val sessionManager: SessionManager,
) : AdminRepository {
    override suspend fun list(): List<AdminAccount> =
        api.list(sessionManager.requireToken()).map { it.toDomain() }

    override suspend fun create(email: String, password: String): AdminAccount =
        api.create(email, password, sessionManager.requireToken()).toDomain()

    override suspend fun delete(id: Int) {
        api.delete(id, sessionManager.requireToken())
    }

    private fun AdminDto.toDomain() = AdminAccount(id = id, email = email, role = role)
}
