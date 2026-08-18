package com.jyodroid.kunasismoayuda.server.error

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

enum class ErrorCode {
    VALIDATION,
    NOT_FOUND,
    UNAUTHORIZED,
    FORBIDDEN,
    UPSTREAM_UNAVAILABLE,
    RATE_LIMITED,
    INTERNAL,
}

class AppException(
    val code: ErrorCode,
    override val message: String,
    val status: HttpStatusCode,
    val details: Map<String, String>? = null,
    /**
     * An *expected*, operational condition (e.g. an upstream is out of credits) rather than a server
     * fault. These log as a one-line WARN with no stack trace, even when the status is 5xx, so they
     * don't look like crashes in the log.
     */
    val expected: Boolean = false,
) : RuntimeException(message)

fun appError(
    code: ErrorCode,
    message: String,
    status: HttpStatusCode,
    details: Map<String, String>? = null,
    expected: Boolean = false,
): AppException = AppException(code, message, status, details, expected)

fun Throwable.toAppException(): AppException = when (this) {
    is AppException -> this
    else -> AppException(
        code = ErrorCode.INTERNAL,
        message = message ?: "Unexpected error",
        status = HttpStatusCode.InternalServerError,
    )
}

@Serializable
data class ErrorResponse(val error: ErrorBody) {
    @Serializable
    data class ErrorBody(
        val code: String,
        val message: String,
        val traceId: String,
        val details: Map<String, String>? = null,
    )
}
