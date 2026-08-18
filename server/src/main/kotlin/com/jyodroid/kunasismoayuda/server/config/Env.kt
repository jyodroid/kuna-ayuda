package com.jyodroid.kunasismoayuda.server.config

/**
 * Production is opt-in via `APP_ENV`/`KTOR_ENV` = `production`|`prod`; anything else (incl. unset) is dev.
 * Shared by the security preflight (fail-fast) and the security response headers (HSTS gating).
 */
internal fun isProductionEnvironment(): Boolean {
    val env = (System.getenv("APP_ENV") ?: System.getenv("KTOR_ENV") ?: "").trim().lowercase()
    return env == "production" || env == "prod"
}
