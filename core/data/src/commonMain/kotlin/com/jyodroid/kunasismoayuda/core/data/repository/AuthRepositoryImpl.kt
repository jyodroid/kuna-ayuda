package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.auth.SessionManager
import com.jyodroid.kunasismoayuda.core.data.remote.AuthApi
import com.jyodroid.kunasismoayuda.core.domain.model.Session
import com.jyodroid.kunasismoayuda.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
) : AuthRepository {

    override val session: StateFlow<Session?> = sessionManager.session

    override val sessionExpired: StateFlow<Boolean> = sessionManager.sessionExpired

    override suspend fun login(email: String, password: String) {
        val response = api.login(email.trim(), password)
        sessionManager.set(token = response.token, role = response.role)
    }

    override fun logout() = sessionManager.clear()
}
