package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.appError
import io.ktor.http.HttpStatusCode
import org.mindrot.jbcrypt.BCrypt

/**
 * Central moderator-password policy — the single place that defines the bcrypt work factor and the
 * minimum length, so auth + admin flows can't drift. Passwords are **hashed** (bcrypt, one-way, salted),
 * never encrypted/recoverable. Existing lower-cost hashes still verify (the cost is embedded in each
 * hash), so raising [BCRYPT_COST] needs no migration.
 */
object PasswordPolicy {
    /** bcrypt work factor: 2^12 rounds (~0.2–0.3s/hash — deliberately slow to resist brute-forcing). */
    const val BCRYPT_COST = 12

    /** Minimum moderator password length. */
    const val MIN_LENGTH = 12

    fun hash(plain: String): String = BCrypt.hashpw(plain, BCrypt.gensalt(BCRYPT_COST))

    /** Throws 400 if the password is shorter than [MIN_LENGTH]. */
    fun requireStrong(plain: String) {
        if (plain.length < MIN_LENGTH) {
            throw appError(
                ErrorCode.VALIDATION,
                "Password must be at least $MIN_LENGTH characters",
                HttpStatusCode.BadRequest,
            )
        }
    }
}
