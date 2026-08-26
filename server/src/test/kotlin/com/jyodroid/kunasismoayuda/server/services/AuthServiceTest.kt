package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.config.JwtConfig
import com.jyodroid.kunasismoayuda.server.domain.models.AdminUser
import com.jyodroid.kunasismoayuda.server.domain.models.Roles
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

class AuthServiceTest {

    private val jwt = JwtConfig(domain = "d", audience = "a", realm = "r", secret = "secret")

    private fun serviceWith(user: AdminUser?): Pair<AuthService, AdminUserRepository> {
        val repo = mockk<AdminUserRepository>(relaxed = true)
        every { repo.findByEmail(any()) } returns user
        return AuthService(repo, jwt) to repo
    }

    private fun user(role: String, password: String) = AdminUser(
        id = 42, email = "mod@example.com", passwordHash = PasswordPolicy.hash(password), role = role,
    )

    @Test
    fun `self-delete removes the account when the password matches`() {
        val (service, repo) = serviceWith(user(Roles.ADMIN, "correct-horse-battery"))
        val deleted = service.deleteOwnAccount("mod@example.com", "correct-horse-battery")
        verify { repo.deleteById(42) }
        assertEquals(42, deleted.id)
    }

    @Test
    fun `self-delete rejects a wrong current password`() {
        val (service, repo) = serviceWith(user(Roles.ADMIN, "correct-horse-battery"))
        assertThrows<Throwable> { service.deleteOwnAccount("mod@example.com", "wrong") }
        verify(exactly = 0) { repo.deleteById(any()) }
    }

    @Test
    fun `self-delete refuses the super-admin owner`() {
        val (service, repo) = serviceWith(user(Roles.SUPERADMIN, "correct-horse-battery"))
        assertThrows<Throwable> { service.deleteOwnAccount("mod@example.com", "correct-horse-battery") }
        verify(exactly = 0) { repo.deleteById(any()) }
    }
}
