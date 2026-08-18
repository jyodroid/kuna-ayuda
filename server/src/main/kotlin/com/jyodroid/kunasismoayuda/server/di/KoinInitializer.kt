package com.jyodroid.kunasismoayuda.server.di

import com.jyodroid.kunasismoayuda.server.config.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    // JWT settings come from application.yaml (jwt.*) + the JWT_SECRET env var. Registered here so
    // AuthService can sign moderator tokens with the same config that configureSecurity() verifies.
    val jwtConfig = JwtConfig.fromConfig(environment.config)
    install(Koin) {
        slf4jLogger()
        modules(appModules + module { single { jwtConfig } })
    }
}
