package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.Session
import kotlinx.coroutines.flow.StateFlow

/**
 * Moderator authentication. Only moderators ever authenticate; the rest of the app is anonymous.
 * The session is held in memory only (a bearer token is never written to disk), so a moderator
 * re-logs in after the app is killed.
 */
interface AuthRepository {
    /** The current moderator session, or null when nobody is logged in. */
    val session: StateFlow<Session?>

    /** Verify credentials against the backend and open a session. Throws on failure. */
    suspend fun login(email: String, password: String)

    /** Drop the in-memory session. */
    fun logout()
}
