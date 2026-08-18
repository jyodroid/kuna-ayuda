package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.AdminUser
import com.jyodroid.kunasismoayuda.server.domain.models.Roles
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt

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
        if (password.length < 8) {
            throw appError(ErrorCode.VALIDATION, "Password must be at least 8 characters", HttpStatusCode.BadRequest)
        }
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
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
}
