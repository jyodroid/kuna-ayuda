package com.jyodroid.kunasismoayuda.server.config

import io.ktor.server.config.ApplicationConfig

/**
 * JWT settings read from `application.yaml` (`jwt.*`). The signing secret comes from the
 * `JWT_SECRET` env var in production. Auth is scaffolded for admin-only moderation flows in M2+;
 * the M1 `/api/quakes` endpoint is public.
 */
data class JwtConfig(
    val domain: String,
    val audience: String,
    val realm: String,
    val secret: String,
) {
    companion object {
        fun fromConfig(config: ApplicationConfig): JwtConfig {
            val jwt = config.config("jwt")
            return JwtConfig(
                domain = jwt.property("domain").getString(),
                audience = jwt.property("audience").getString(),
                realm = jwt.property("realm").getString(),
                secret = System.getenv("JWT_SECRET")
                    ?: jwt.propertyOrNull("secret")?.getString()
                    ?: "dev-insecure-secret-change-me",
            )
        }
    }
}
