package com.jyodroid.kunasismoayuda.server.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
    val role: String,
)

/** Self-service password change (any moderator, via the web console). */
@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)
