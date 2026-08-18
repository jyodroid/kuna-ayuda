package com.jyodroid.kunasismoayuda.core.data.auth

import com.jyodroid.kunasismoayuda.core.domain.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Raised when a moderator-only call is attempted without an active session. */
class NotAuthenticatedException : IllegalStateException("Not authenticated")

/**
 * Holds the moderator's bearer token in memory (never on disk) and the derived [Session] the UI
 * observes. Shared between the auth repository (which sets it on login) and the board repository
 * (which reads the token to authorize moderation calls).
 */
class SessionManager {
    private var token: String? = null

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    fun set(token: String, role: String) {
        this.token = token
        _session.value = Session(role = role)
    }

    fun clear() {
        token = null
        _session.value = null
    }

    /** The current bearer token, or throw if nobody is logged in. */
    fun requireToken(): String = token ?: throw NotAuthenticatedException()
}
