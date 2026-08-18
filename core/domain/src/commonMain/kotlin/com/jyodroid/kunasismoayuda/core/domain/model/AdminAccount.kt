package com.jyodroid.kunasismoayuda.core.domain.model

/** An admin account as seen by the super-admin console. */
data class AdminAccount(
    val id: Int,
    val email: String,
    val role: String,
) {
    val isSuperAdmin: Boolean get() = role == ROLE_SUPERADMIN

    companion object {
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_SUPERADMIN = "SUPERADMIN"
    }
}
