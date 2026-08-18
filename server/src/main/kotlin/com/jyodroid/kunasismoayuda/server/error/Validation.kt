package com.jyodroid.kunasismoayuda.server.error

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation

/**
 * Request-body validation. Rules are added per-DTO as write endpoints arrive in M2+.
 */
fun Application.configureValidation() {
    install(RequestValidation) {
        // e.g. validate<HelpRequest> { ... } once write endpoints exist.
    }
}
