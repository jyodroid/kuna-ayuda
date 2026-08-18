package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * An authenticated moderator session. Only the role is exposed to the UI — the JWT itself stays in
 * the data layer and is never surfaced to presentation. Regular app users have no session (null).
 */
data class Session(
    val role: String, // ADMIN
)
