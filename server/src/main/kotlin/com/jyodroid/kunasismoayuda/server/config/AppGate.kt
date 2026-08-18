package com.jyodroid.kunasismoayuda.server.config

import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.ErrorResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.request.header
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import java.security.MessageDigest
import java.util.UUID

private const val APP_KEY_HEADER = "X-App-Key"

/**
 * Optional "our apps only" gate. When `APP_CLIENT_KEY` is set, every request under the `/api/` prefix
 * must carry an `X-App-Key` header matching it — the KMP client sends it automatically (see core:data
 * `ServerConfig.APP_CLIENT_KEY` + `HttpClientFactory`). Unset ⇒ gate disabled (dev/open mode), mirroring
 * the other env-gated toggles.
 *
 * This is a **deterrent**, not identity proof: the key ships inside the app binary and can be extracted,
 * so it filters casual scanners/bots/browser callers but not a determined reverse-engineer. The real
 * "only our app" guarantee is platform attestation (Play Integrity / App Attest) — deferred.
 */
fun Application.configureAppGate() {
    val key = System.getenv("APP_CLIENT_KEY")?.trim()
    if (key.isNullOrBlank()) {
        log.info("APP_CLIENT_KEY unset — app gate disabled (open mode).")
        return
    }
    val keyBytes = key.toByteArray(Charsets.UTF_8)
    log.info("APP_CLIENT_KEY set — /api/** requires a matching $APP_KEY_HEADER header.")

    intercept(ApplicationCallPipeline.Plugins) {
        val path = call.request.path()
        // Only guard the API surface; let health/root and CORS preflight (OPTIONS) through.
        if (!path.startsWith("/api/") || call.request.httpMethod == HttpMethod.Options) {
            return@intercept
        }
        val provided = call.request.header(APP_KEY_HEADER)?.toByteArray(Charsets.UTF_8)
        val ok = provided != null && MessageDigest.isEqual(provided, keyBytes)
        if (!ok) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    ErrorResponse.ErrorBody(
                        code = ErrorCode.UNAUTHORIZED.name,
                        message = "Missing or invalid application key",
                        traceId = UUID.randomUUID().toString(),
                    ),
                ),
            )
            finish()
        }
    }
}
