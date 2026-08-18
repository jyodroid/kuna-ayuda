package com.jyodroid.kunasismoayuda.server.config

import io.ktor.server.application.Application
import io.ktor.server.application.log

/** The fallback secret baked into [JwtConfig] — if this is what's in use, tokens are forgeable. */
private const val INSECURE_JWT_SECRET = "dev-insecure-secret-change-me"

/** Below this length an admin password is too weak to resist an online guess (login IS rate-limited). */
private const val MIN_ADMIN_PASSWORD_LENGTH = 12

/**
 * Startup safety net against production misconfiguration. The one catastrophic gap — a missing or
 * default `JWT_SECRET`, which lets anyone mint ADMIN/SUPERADMIN tokens — **aborts boot in production**
 * (`APP_ENV`/`KTOR_ENV` = `production`|`prod`): failing loudly is far better than silently serving
 * forgeable admin tokens. Softer gaps (open CORS, disabled app-key gate, weak/absent admin password)
 * are logged as warnings in every environment so they're visible in the boot log without blocking dev.
 *
 * Call this **first** in `Application.module()` — before auth/routing are installed.
 */
fun Application.configureSecurityPreflight() {
    val prod = isProductionEnvironment()
    val warnings = mutableListOf<String>()
    val fatal = mutableListOf<String>()

    // Effective JWT secret = env JWT_SECRET, else yaml jwt.secret, else the insecure default (JwtConfig).
    val jwtSecret = System.getenv("JWT_SECRET")?.trim()
        ?: environment.config.propertyOrNull("jwt.secret")?.getString()?.trim()
    if (jwtSecret.isNullOrBlank() || jwtSecret == INSECURE_JWT_SECRET) {
        val msg = "JWT_SECRET is unset or the insecure dev default — JWTs are forgeable (anyone can mint ADMIN/SUPERADMIN)."
        if (prod) fatal += msg else warnings += "$msg Set JWT_SECRET before deploying."
    }

    if (System.getenv("ALLOWED_ORIGIN").isNullOrBlank()) {
        warnings += "ALLOWED_ORIGIN unset — CORS falls back to anyHost() (any browser origin). Set it in prod."
    }
    if (System.getenv("APP_CLIENT_KEY").isNullOrBlank()) {
        warnings += "APP_CLIENT_KEY unset — the app-only gate is disabled (open API). Set it in prod."
    }
    val adminPw = System.getenv("ADMIN_PASSWORD")?.trim()
    when {
        adminPw.isNullOrBlank() ->
            warnings += "ADMIN_PASSWORD unset — no moderator is seeded; login returns 401 until one exists."
        adminPw.length < MIN_ADMIN_PASSWORD_LENGTH ->
            warnings += "ADMIN_PASSWORD is short (<$MIN_ADMIN_PASSWORD_LENGTH chars) — use a strong moderator password."
    }

    warnings.forEach { log.warn("[security] $it") }

    if (fatal.isNotEmpty()) {
        fatal.forEach { log.error("[security] FATAL: $it") }
        error(
            buildString {
                append("Refusing to start in production with insecure security config:\n")
                fatal.forEach { append(" - ").append(it).append('\n') }
                append("Set the required env var(s), or unset APP_ENV/KTOR_ENV to run in dev mode.")
            },
        )
    }

    log.info("[security] preflight complete (production=$prod, ${warnings.size} warning(s)).")
}
