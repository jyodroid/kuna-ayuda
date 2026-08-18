package com.jyodroid.kunasismoayuda.server.config

import com.jyodroid.kunasismoayuda.server.domain.models.Roles
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject
import org.mindrot.jbcrypt.BCrypt

/**
 * Bootstraps the first moderator from env vars so a fresh deployment has someone who can log in.
 * Set ADMIN_EMAIL + ADMIN_PASSWORD; the account is created once (idempotent) with a bcrypt-hashed
 * password. If they're unset, no moderator exists yet and login simply returns 401 for everyone —
 * the public app is unaffected. Add more moderators later via SQL or a future admin endpoint.
 */
fun Application.seedAdminUser() {
    val email = System.getenv("ADMIN_EMAIL")?.trim()?.lowercase()
    val password = System.getenv("ADMIN_PASSWORD")
    if (email.isNullOrBlank() || password.isNullOrBlank()) {
        environment.log.info("No ADMIN_EMAIL/ADMIN_PASSWORD set — skipping moderator seeding.")
        return
    }
    val repository by inject<AdminUserRepository>()
    // The env-seeded owner account is the SUPERADMIN — the only role that can manage other admins.
    val created = repository.createIfAbsent(email, BCrypt.hashpw(password, BCrypt.gensalt()), Roles.SUPERADMIN)
    if (created) {
        environment.log.info("Seeded super-admin account: $email")
    } else {
        environment.log.info("Admin account already present: $email")
    }
}
