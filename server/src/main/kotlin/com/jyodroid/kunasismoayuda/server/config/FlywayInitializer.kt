package com.jyodroid.kunasismoayuda.server.config

import org.flywaydb.core.Flyway

/**
 * Runs Flyway migrations from `src/main/resources/db/migration`. Only invoked when a database
 * is configured. Migration files (`V{n}__description.sql`) arrive with the moderated-data
 * features in M2+.
 */
object FlywayInitializer {
    fun runFlywayMigrations() {
        val (jdbcUrl, user, password) = DbEnvParser.resolve()
        // Pass this class's classloader explicitly. Under `java -jar` on the shaded fat jar (how
        // `:server:stage`/Heroku runs), Flyway's default thread-context classloader can't enumerate
        // `db/migration` inside the shaded jar — it resolves zero migrations and silently skips new
        // ones. The app classloader always sees the packaged resources.
        Flyway.configure(FlywayInitializer::class.java.classLoader)
            .dataSource(jdbcUrl, user, password)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
            .migrate()
    }
}
