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

    // True when the session was dropped because the token expired/was rejected (401), not by an
    // explicit sign-out. Lets the login screen explain why the moderator is back at the form.
    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    fun set(token: String, role: String) {
        this.token = token
        _session.value = Session(role = role)
        _sessionExpired.value = false
    }

    /** Explicit sign-out (user tapped "Sign out"): clears the session without the expiry flag. */
    fun clear() {
        token = null
        _session.value = null
        _sessionExpired.value = false
    }

    /**
     * The token was rejected (401) — expired or revoked. Clears the session and flags expiry so the
     * UI can show a "session expired, sign in again" hint. No-op if nobody was logged in.
     */
    fun expire() {
        if (token == null && _session.value == null) return
        token = null
        _session.value = null
        _sessionExpired.value = true
    }

    /** The current bearer token, or throw if nobody is logged in. */
    fun requireToken(): String = token ?: throw NotAuthenticatedException()
}
