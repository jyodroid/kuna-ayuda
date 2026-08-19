package com.jyodroid.kunasismoayuda.server.config

import java.net.URI

/**
 * Resolves database connection settings from **project-scoped** environment variables so a
 * generic `DATABASE_URL` exported for a *different* project can never hijack this server:
 * `DATABASE_URL_KUNA` (Heroku-style `postgres://user:password@host:port/db`) takes precedence;
 * otherwise fall back to this project's local Postgres using `DB_PASSWORD_KUNA`.
 * In Milestone 1 no database is required, so [isConfigured] gates startup.
 */
object DbEnvParser {

    data class DbSettings(val jdbcUrl: String, val user: String, val password: String)

    private const val LOCAL_JDBC = "jdbc:postgresql://localhost:5432/kuna_sismo_db"
    private const val LOCAL_USER = "kuna_user"

    fun isConfigured(): Boolean = isConfigured(System::getenv)

    /** Testable seam — takes an env lookup instead of reading the process environment directly. */
    fun isConfigured(getenv: (String) -> String?): Boolean {
        val hasUrl = !getenv("DATABASE_URL_KUNA").isNullOrBlank()
        val hasLocalPassword = !getenv("DB_PASSWORD_KUNA").isNullOrBlank()
        return hasUrl || hasLocalPassword
    }

    fun resolve(): DbSettings = resolve(System::getenv)

    /** Testable seam — takes an env lookup instead of reading the process environment directly. */
    fun resolve(getenv: (String) -> String?): DbSettings {
        val dbUrl = getenv("DATABASE_URL_KUNA")
        return if (!dbUrl.isNullOrBlank()) {
            val uri = URI(dbUrl)
            val userInfo = uri.userInfo.split(":")
            val port = if (uri.port != -1) uri.port else 5432 // Heroku URLs include it; default defensively
            val host = uri.host
            // Managed Postgres (Heroku, etc.) requires SSL; local dev does not. `sslmode=require` accepts
            // the provider's cert without full CA verification — Heroku Postgres uses self-signed certs, so
            // `verify-full` would fail. Skip it for localhost, where the server has no TLS.
            val isLocal = host == "localhost" || host == "127.0.0.1"
            val sslParam = if (isLocal) "" else "?sslmode=require"
            DbSettings(
                jdbcUrl = "jdbc:postgresql://$host:$port${uri.path}$sslParam",
                user = userInfo[0],
                password = userInfo.getOrElse(1) { "" },
            )
        } else {
            DbSettings(
                jdbcUrl = LOCAL_JDBC,
                user = LOCAL_USER,
                password = getenv("DB_PASSWORD_KUNA") ?: "",
            )
        }
    }
}
