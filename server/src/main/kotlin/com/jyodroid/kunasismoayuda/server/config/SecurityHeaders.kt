package com.jyodroid.kunasismoayuda.server.config

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call

/**
 * Adds hardening response headers to every reply. Cheap defence-in-depth that matters mostly for the
 * browser attack surface (the native apps ignore them), and future-proofs the planned landing page:
 * - `X-Content-Type-Options: nosniff` — no MIME sniffing.
 * - `X-Frame-Options: DENY` + CSP `frame-ancestors 'none'` — anti-clickjacking (belt + suspenders for
 *   old and new browsers). CSP is limited to framing so it never blocks the landing page's own assets.
 * - `Referrer-Policy: no-referrer` — don't leak URLs onward.
 * - `Strict-Transport-Security` — **production only** (served over HTTPS via the platform TLS proxy);
 *   omitted in dev so plain-HTTP localhost is never pinned.
 * Set once per call, before the handler runs.
 */
fun Application.configureSecurityHeaders() {
    val prod = isProductionEnvironment()
    intercept(ApplicationCallPipeline.Plugins) {
        val headers = call.response.headers
        if (headers["X-Content-Type-Options"] == null) {
            headers.append("X-Content-Type-Options", "nosniff")
            headers.append("X-Frame-Options", "DENY")
            headers.append("Referrer-Policy", "no-referrer")
            headers.append("Content-Security-Policy", "frame-ancestors 'none'")
            if (prod) {
                headers.append("Strict-Transport-Security", "max-age=63072000; includeSubDomains")
            }
        }
    }
}
