package com.jyodroid.kunasismoayuda.server.routes.dto

import com.jyodroid.kunasismoayuda.server.domain.models.AdminUser
import kotlinx.serialization.Serializable

/** Public shape of an admin account — never includes the password hash. */
@Serializable
data class AdminDto(
    val id: Int,
    val email: String,
    val role: String,
)

@Serializable
data class CreateAdminRequest(
    val email: String,
    val password: String,
)

fun AdminUser.toDto() = AdminDto(id = id, email = email, role = role)
