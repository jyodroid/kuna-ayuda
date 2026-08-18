package com.jyodroid.kunasismoayuda.server.config

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.seconds

/** Per-IP limiter for user-submitted content (aid-board / search / photo posts), to curb spam/abuse. */
val BoardRateLimit = RateLimitName("board")

/** Per-IP limiter for moderator login — its own bucket so credential brute-forcing is throttled
 *  independently of board traffic (board spam must not lock out logins, and vice versa). */
val LoginRateLimit = RateLimitName("login")

/**
 * The rate-limit bucket key: the client IP. With [io.ktor.server.plugins.forwardedheaders.XForwardedHeaders]
 * installed (see [configureRateLimit]), `origin.remoteHost` reflects the real client via the proxy's
 * `X-Forwarded-For` (correct on Heroku); with no proxy it's the socket peer. A blank address folds into
 * one shared "unknown" bucket so a missing IP can't dodge the limit by scattering across null keys.
 */
private fun ApplicationCall.clientKey(): String =
    request.origin.remoteHost.ifBlank { "unknown" }

fun Application.configureRateLimit() {
    // Trust the proxy's X-Forwarded-For so per-IP limiting keys on the real client, not Heroku's router.
    // Safe here because the app is deployed behind a trusted proxy; without one it degrades to the peer IP.
    install(io.ktor.server.plugins.forwardedheaders.XForwardedHeaders)

    install(RateLimit) {
        // Aid-board / search / photo posting: 5 writes per minute, PER IP.
        register(BoardRateLimit) {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
            requestKey { call -> call.clientKey() }
        }
        // Login: stricter — 10 attempts per 5 minutes, PER IP (brute-force deterrent, still humane for typos).
        register(LoginRateLimit) {
            rateLimiter(limit = 10, refillPeriod = 300.seconds)
            requestKey { call -> call.clientKey() }
        }
    }
}
