package com.jyodroid.kunasismoayuda.server.config

import com.jyodroid.kunasismoayuda.server.error.ErrorCode
import com.jyodroid.kunasismoayuda.server.error.ErrorResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.request.contentLength
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import java.util.UUID

/**
 * Max size for a JSON (non-multipart) request body. Every write on this API — board/search/SOS posts,
 * login, the classify paste — is small text, so 128 KB is generous; the cap exists purely to stop a
 * client from exhausting memory with a multi-MB body. `/api/photos` is exempt (multipart, enforces its
 * own 3 MB cap in `PhotoRoutes`).
 */
private const val MAX_JSON_BODY_BYTES = 128L * 1024L

/**
 * Rejects oversized JSON request bodies early (by `Content-Length`) with **413 Payload Too Large**,
 * before routing/`receive()` reads them into memory. Only guards `/api/` POST/PUT and skips
 * `/api/photos` (multipart, self-limited). Bodies sent without a `Content-Length` (chunked) slip past
 * this header check — an accepted limitation; the realistic abuse (a large body with Content-Length
 * set) is covered. Install after the app gate.
 */
fun Application.configureRequestSizeLimit() {
    intercept(ApplicationCallPipeline.Plugins) {
        val request = call.request
        val method = request.httpMethod
        if (method != HttpMethod.Post && method != HttpMethod.Put) return@intercept

        val path = request.path()
        if (!path.startsWith("/api/") || path.startsWith("/api/photos")) return@intercept

        val length = request.contentLength()
        if (length != null && length > MAX_JSON_BODY_BYTES) {
            call.respond(
                HttpStatusCode.PayloadTooLarge,
                ErrorResponse(
                    ErrorResponse.ErrorBody(
                        code = ErrorCode.VALIDATION.name,
                        message = "Request body too large (max ${MAX_JSON_BODY_BYTES / 1024} KB)",
                        traceId = UUID.randomUUID().toString(),
                    ),
                ),
            )
            finish()
        }
    }
}
