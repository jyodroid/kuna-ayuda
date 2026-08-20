package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.AdminUser
import com.jyodroid.kunasismoayuda.server.domain.models.Roles
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import io.ktor.http.HttpStatusCode

/**
 * Admin-account management, reachable only by a SUPERADMIN (enforced at the route). Created accounts
 * are always plain [Roles.ADMIN] — a superadmin cannot mint another superadmin through the API, which
 * keeps the env-seeded owner the sole account that can manage admins.
 */
class AdminService(private val repository: AdminUserRepository) {

    fun list(): List<AdminUser> = repository.listAll()

    fun create(email: String, password: String): AdminUser {
        val normalized = email.trim().lowercase()
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw appError(ErrorCode.VALIDATION, "A valid email is required", HttpStatusCode.BadRequest)
        }
        PasswordPolicy.requireStrong(password)
        val hash = PasswordPolicy.hash(password)
        return repository.create(normalized, hash, Roles.ADMIN)
            ?: throw appError(ErrorCode.VALIDATION, "An admin with that email already exists", HttpStatusCode.Conflict)
    }

    /** Delete an admin. Guards: can't delete yourself, and can't delete a SUPERADMIN (owner). */
    fun delete(id: Int, callerEmail: String?) {
        val target = repository.findById(id)
            ?: throw appError(ErrorCode.NOT_FOUND, "Admin not found", HttpStatusCode.NotFound)
        if (Roles.isSuperAdmin(target.role)) {
            throw appError(ErrorCode.FORBIDDEN, "The super-admin account can't be removed", HttpStatusCode.Forbidden)
        }
        if (callerEmail != null && target.email.equals(callerEmail, ignoreCase = true)) {
            throw appError(ErrorCode.FORBIDDEN, "You can't delete your own account", HttpStatusCode.Forbidden)
        }
        repository.deleteById(id)
    }

    /** Enable/disable an account. Guards: can't disable yourself or the SUPERADMIN owner. Returns the target. */
    fun setEnabled(id: Int, enabled: Boolean, callerEmail: String?): AdminUser {
        val target = repository.findById(id)
            ?: throw appError(ErrorCode.NOT_FOUND, "Admin not found", HttpStatusCode.NotFound)
        if (!enabled && Roles.isSuperAdmin(target.role)) {
            throw appError(ErrorCode.FORBIDDEN, "The super-admin account can't be disabled", HttpStatusCode.Forbidden)
        }
        if (!enabled && callerEmail != null && target.email.equals(callerEmail, ignoreCase = true)) {
            throw appError(ErrorCode.FORBIDDEN, "You can't disable your own account", HttpStatusCode.Forbidden)
        }
        repository.setEnabled(id, enabled)
        return target.copy(enabled = enabled)
    }

    /** Super-admin reset of another account's password (no current password required). Returns the target. */
    fun resetPassword(id: Int, newPassword: String): AdminUser {
        val target = repository.findById(id)
            ?: throw appError(ErrorCode.NOT_FOUND, "Admin not found", HttpStatusCode.NotFound)
        PasswordPolicy.requireStrong(newPassword)
        repository.updatePassword(id, PasswordPolicy.hash(newPassword))
        return target
    }
}
