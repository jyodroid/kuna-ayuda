package com.jyodroid.kunasismoayuda.server.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DbEnvParserTest {

    private fun env(vararg pairs: Pair<String, String?>): (String) -> String? {
        val map = pairs.toMap()
        return { map[it] }
    }

    @Test
    fun heroku_style_url_becomes_a_jdbc_url_with_sslmode_require() {
        val settings = DbEnvParser.resolve(
            env("DATABASE_URL_KUNA" to "postgres://u123:secretpw@ec2-1-2-3-4.compute.amazonaws.com:5432/dbname"),
        )
        assertEquals(
            "jdbc:postgresql://ec2-1-2-3-4.compute.amazonaws.com:5432/dbname?sslmode=require",
            settings.jdbcUrl,
        )
        assertEquals("u123", settings.user)
        assertEquals("secretpw", settings.password)
    }

    @Test
    fun localhost_url_omits_sslmode() {
        val settings = DbEnvParser.resolve(
            env("DATABASE_URL_KUNA" to "postgres://kuna_user:kunapass@localhost:5432/kuna_sismo_db"),
        )
        assertEquals("jdbc:postgresql://localhost:5432/kuna_sismo_db", settings.jdbcUrl)
        assertFalse(settings.jdbcUrl.contains("sslmode"), "local dev Postgres has no TLS")
    }

    @Test
    fun missing_port_defaults_to_5432() {
        val settings = DbEnvParser.resolve(
            env("DATABASE_URL_KUNA" to "postgres://u:p@db.example.com/mydb"),
        )
        assertEquals("jdbc:postgresql://db.example.com:5432/mydb?sslmode=require", settings.jdbcUrl)
    }

    @Test
    fun falls_back_to_local_settings_when_no_url() {
        val settings = DbEnvParser.resolve(env("DB_PASSWORD_KUNA" to "localpw"))
        assertEquals("jdbc:postgresql://localhost:5432/kuna_sismo_db", settings.jdbcUrl)
        assertEquals("kuna_user", settings.user)
        assertEquals("localpw", settings.password)
    }

    @Test
    fun is_configured_reflects_either_env_var() {
        assertTrue(DbEnvParser.isConfigured(env("DATABASE_URL_KUNA" to "postgres://u:p@h:5432/d")))
        assertTrue(DbEnvParser.isConfigured(env("DB_PASSWORD_KUNA" to "x")))
        assertFalse(DbEnvParser.isConfigured(env("SOMETHING_ELSE" to "y")))
        assertFalse(DbEnvParser.isConfigured(env("DATABASE_URL_KUNA" to "  "))) // blank ⇒ not configured
    }
}
