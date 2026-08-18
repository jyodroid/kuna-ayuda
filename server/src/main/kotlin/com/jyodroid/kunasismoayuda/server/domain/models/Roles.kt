package com.jyodroid.kunasismoayuda.server.domain.models

/**
 * Admin roles. [ADMIN] can moderate content (shelters, board posts); [SUPERADMIN] additionally
 * manages other admin accounts. The env-seeded owner account is a SUPERADMIN.
 */
object Roles {
    const val ADMIN = "ADMIN"
    const val SUPERADMIN = "SUPERADMIN"

    /** Both roles have moderator privileges. */
    fun isAdmin(role: String?): Boolean = role == ADMIN || role == SUPERADMIN
    fun isSuperAdmin(role: String?): Boolean = role == SUPERADMIN
}
