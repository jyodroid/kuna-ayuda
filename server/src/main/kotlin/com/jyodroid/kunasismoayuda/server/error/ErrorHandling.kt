package com.jyodroid.kunasismoayuda.server.error

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.UUID

fun Application.configureErrorHandling() {
    val logger = LoggerFactory.getLogger("ErrorHandler")

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val appEx = cause.toAppException()
            val traceId = call.callId ?: UUID.randomUUID().toString()
            MDC.put("traceId", traceId)
            try {
                when {
                    // Expected operational conditions (e.g. upstream out of credits): one-line WARN,
                    // no stack — they aren't server faults even when the status is 5xx.
                    appEx.expected -> logger.warn("Handled ${appEx.code}: ${appEx.message}")
                    appEx.status.value in 400..499 -> logger.warn("Handled ${appEx.code}: ${appEx.message}", cause)
                    else -> logger.error("Unhandled ${appEx.code}: ${appEx.message}", cause)
                }
                call.response.headers.append("X-Trace-Id", traceId)
                call.respond(
                    status = appEx.status,
                    message = ErrorResponse(
                        ErrorResponse.ErrorBody(
                            code = appEx.code.name,
                            message = appEx.message,
                            traceId = traceId,
                            details = appEx.details,
                        ),
                    ),
                )
            } finally {
                MDC.remove("traceId")
            }
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            val traceId = call.callId ?: UUID.randomUUID().toString()
            call.response.headers.append("X-Trace-Id", traceId)
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    ErrorResponse.ErrorBody(
                        code = ErrorCode.NOT_FOUND.name,
                        message = "Route not found",
                        traceId = traceId,
                    ),
                ),
            )
        }
    }
}
