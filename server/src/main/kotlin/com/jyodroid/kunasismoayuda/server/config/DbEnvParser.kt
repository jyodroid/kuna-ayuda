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

    fun isConfigured(): Boolean {
        val hasUrl = !System.getenv("DATABASE_URL_KUNA").isNullOrBlank()
        val hasLocalPassword = !System.getenv("DB_PASSWORD_KUNA").isNullOrBlank()
        return hasUrl || hasLocalPassword
    }

    fun resolve(): DbSettings {
        val dbUrl = System.getenv("DATABASE_URL_KUNA")
        return if (!dbUrl.isNullOrBlank()) {
            val uri = URI(dbUrl)
            val userInfo = uri.userInfo.split(":")
            DbSettings(
                jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}",
                user = userInfo[0],
                password = userInfo.getOrElse(1) { "" },
            )
        } else {
            DbSettings(
                jdbcUrl = LOCAL_JDBC,
                user = LOCAL_USER,
                password = System.getenv("DB_PASSWORD_KUNA") ?: "",
            )
        }
    }
}
