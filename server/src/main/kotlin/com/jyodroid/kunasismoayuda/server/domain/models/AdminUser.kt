package com.jyodroid.kunasismoayuda.server.domain.models

/** A moderator account. `passwordHash` is a bcrypt hash — the plaintext is never persisted. */
data class AdminUser(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val role: String, // ADMIN
)
