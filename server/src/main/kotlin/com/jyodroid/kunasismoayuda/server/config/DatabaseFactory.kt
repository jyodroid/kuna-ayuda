package com.jyodroid.kunasismoayuda.server.config

import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.exposed.sql.Database

/**
 * Connects Exposed to Postgres. Only invoked when [DbEnvParser.isConfigured] is true.
 */
object DatabaseFactory {
    /** True once a database connection has been established. Repositories use this to no-op
     * gracefully when the server runs without a database (M1 dev mode). */
    @Volatile
    var initialized: Boolean = false
        private set

    fun init(environment: ApplicationEnvironment) {
        val driver = environment.config
            .propertyOrNull("ktor.database.driver")?.getString()
            ?: "org.postgresql.Driver"
        val (jdbcUrl, user, password) = DbEnvParser.resolve()
        Database.connect(url = jdbcUrl, driver = driver, user = user, password = password)
        initialized = true
    }
}
