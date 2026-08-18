package com.jyodroid.kunasismoayuda.server

import com.jyodroid.kunasismoayuda.server.config.DatabaseFactory
import com.jyodroid.kunasismoayuda.server.config.DbEnvParser
import com.jyodroid.kunasismoayuda.server.config.FlywayInitializer
import com.jyodroid.kunasismoayuda.server.config.seedAdminUser
import com.jyodroid.kunasismoayuda.server.config.startDisasterIngestion
import com.jyodroid.kunasismoayuda.server.config.startExpirySweep
import com.jyodroid.kunasismoayuda.server.config.configureAppGate
import com.jyodroid.kunasismoayuda.server.config.configureCors
import com.jyodroid.kunasismoayuda.server.config.configureLogging
import com.jyodroid.kunasismoayuda.server.config.configureRateLimit
import com.jyodroid.kunasismoayuda.server.config.configureRequestSizeLimit
import com.jyodroid.kunasismoayuda.server.config.configureSecurity
import com.jyodroid.kunasismoayuda.server.config.configureSecurityHeaders
import com.jyodroid.kunasismoayuda.server.config.configureSecurityPreflight
import com.jyodroid.kunasismoayuda.server.di.configureKoin
import com.jyodroid.kunasismoayuda.server.error.configureErrorHandling
import com.jyodroid.kunasismoayuda.server.error.configureValidation
import com.jyodroid.kunasismoayuda.server.routes.configureRouting
import com.jyodroid.kunasismoayuda.server.serializer.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Fail-fast on catastrophic prod misconfig (forgeable JWT secret) before anything else boots;
    // warn on softer gaps. No-op-ish in dev (warnings only).
    configureSecurityPreflight()
    configureKoin()
    configureSecurity()
    configureLogging()
    configureSerialization()
    configureRateLimit()
    configureRouting()
    configureCors()
    // Hardening response headers (nosniff, anti-clickjacking, referrer, HSTS in prod).
    configureSecurityHeaders()
    // Optional "our apps only" gate (env APP_CLIENT_KEY); no-op when unset.
    configureAppGate()
    // Reject oversized JSON bodies (413) before they're read into memory.
    configureRequestSizeLimit()
    configureValidation()
    configureErrorHandling()

    // Diagnostic: is the NASA FIRMS point feed active? (Never logs the key value.) If this says
    // "disabled", GET /api/fires falls back to coarse GDACS events — set FIRMS_MAP_KEY on THIS process.
    val firmsKeySet = !System.getenv("FIRMS_MAP_KEY").isNullOrBlank()
    environment.log.info(
        "[fires] FIRMS active-fire feed ${if (firmsKeySet) "ENABLED (FIRMS_MAP_KEY present)" else "DISABLED (FIRMS_MAP_KEY unset) — using GDACS fallback"}.",
    )

    // Persistence is optional for Milestone 1 (the /api/quakes endpoint reads upstream feeds,
    // not Postgres). It only spins up when a database is actually configured, so the server
    // runs standalone in dev. Moderated-data tables (shelters, help/SOS requests) arrive in M2+.
    if (DbEnvParser.isConfigured()) {
        FlywayInitializer.runFlywayMigrations()
        DatabaseFactory.init(environment)
        environment.log.info("Database configured — Flyway migrations applied.")
        // Bootstrap the first moderator (from ADMIN_EMAIL/ADMIN_PASSWORD) so login is usable.
        seedAdminUser()
        // Pull external feeds (GDACS earthquakes + ReliefWeb reports) on startup, then on a schedule.
        startDisasterIngestion()
        // Auto-close stale aid-board posts / Lost & Found reports (>30 days) on startup, then daily.
        startExpirySweep()
    } else {
        environment.log.info("No DATABASE_URL/DB_PASSWORD configured — running without a database (M1 mode).")
    }
}
