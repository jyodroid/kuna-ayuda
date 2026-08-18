package com.jyodroid.kunasismoayuda.server.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jyodroid.kunasismoayuda.server.config.JwtConfig
import com.jyodroid.kunasismoayuda.server.domain.models.AdminUser
import com.jyodroid.kunasismoayuda.server.domain.repositories.AdminUserRepository
import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

/**
 * Moderator authentication. This is the ONLY place a JWT is minted, and it only ever mints tokens
 * for accounts in `admin_users` — regular app users never authenticate. The issued token carries a
 * `role=ADMIN` claim that the board's admin routes enforce via requireAdmin(). Passwords are
 * verified against a bcrypt hash; the plaintext is never stored or logged.
 */
class AuthService(
    private val repository: AdminUserRepository,
    private val jwtConfig: JwtConfig,
) {
    /** token + the account's role, so the client can gate super-admin-only UI. */
    data class LoginResult(val token: String, val role: String)

    /** Verify credentials and return a signed JWT + role. Fails with 401 on any mismatch. */
    fun login(email: String, password: String): LoginResult {
        val normalized = email.trim().lowercase()
        val user = repository.findByEmail(normalized)
        // Always run a bcrypt check to keep the response time uniform whether or not the email exists.
        val ok = if (user != null) {
            BCrypt.checkpw(password, user.passwordHash)
        } else {
            BCrypt.checkpw(password, DUMMY_HASH)
            false
        }
        if (!ok || user == null) {
            throw appError(ErrorCode.UNAUTHORIZED, "Invalid credentials", HttpStatusCode.Unauthorized)
        }
        return LoginResult(token = issueToken(user), role = user.role)
    }

    private fun issueToken(user: AdminUser): String =
        JWT.create()
            .withIssuer(jwtConfig.domain)
            .withAudience(jwtConfig.audience)
            .withSubject(user.email)
            .withClaim("role", user.role)
            .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_TTL_MS))
            .sign(Algorithm.HMAC256(jwtConfig.secret))

    companion object {
        // A moderator's working session; they re-login when it lapses.
        private const val TOKEN_TTL_MS = 12L * 60 * 60 * 1000 // 12 hours
        // A fixed valid bcrypt hash of a throwaway value, used only to equalize timing on unknown emails.
        private val DUMMY_HASH = BCrypt.hashpw("timing-equalizer", BCrypt.gensalt())
    }
}
