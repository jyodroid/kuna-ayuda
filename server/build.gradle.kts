plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.shadow)
}

group = "com.jyodroid.kunasismoayuda"
version = "1.3.0"

application {
    mainClass.set("com.jyodroid.kunasismoayuda.server.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.server.client)
    implementation(libs.logback.classic)
    implementation(libs.bundles.server.koin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // Persistence (wired for M2+; DB init is gated off until DATABASE_URL is set)
    implementation(libs.bundles.server.exposed)
    implementation(libs.bundles.server.flyway)
    implementation(libs.postgres.db)
    implementation(libs.jbcrypt)

    testImplementation(libs.bundles.server.testing)
    testImplementation(libs.ktor.client.mock) // MockEngine for upstream-parser tests (no network)
    testRuntimeOnly(libs.junit.jupiter.engine)
    // Gradle 9 requires the JUnit Platform launcher on the test runtime classpath explicitly.
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    shadowJar {
        archiveBaseName.set("server")
        archiveClassifier.set("all")
        archiveVersion.set("$version")
        // Flyway 11 registers its plugins (SQL resolvers + the config extensions that define the
        // V__/R__/U__ migration naming) via META-INF/services/org.flywaydb.core.extensibility.Plugin,
        // and BOTH flyway-core (27 entries) and flyway-database-postgresql (3 entries) ship one. The
        // default service merge collapsed them to postgres-only, so ResourceNameParser had no prefixes
        // and rejected every migration in the fat jar (0 applied → broken Heroku deploys). We ship the
        // fully-merged list at server/src/main/resources/META-INF/services/…Plugin and exclude that path
        // from the service-file transformer so our complete, correctly-newlined file is packaged as-is
        // (not overwritten by postgres, and not glued together by append).
        mergeServiceFiles {
            // exclude() patterns are relative to the transformer's "META-INF/services" base path.
            exclude("org.flywaydb.core.extensibility.Plugin")
        }
    }

    test {
        useJUnitPlatform()
    }

    // Heroku-style deploy hook: `./gradlew :server:stage` builds the fat jar
    register("stage") {
        description = "Prepare the server for deployment (builds the fat jar)."
        dependsOn("shadowJar")
    }

    // --- Landing site (Vite) ---
    // The built site lives in src/main/resources/web and is packaged by processResources/shadowJar like
    // any other resource. `buildLanding` is a MANUAL developer task (needs Node/npm) that regenerates it;
    // it is deliberately NOT wired into processResources so Heroku (JVM buildpack, no Node) just packages
    // whatever web/ is already present. Run `./gradlew :server:buildLanding` after editing landing/.
    val landingDir = rootProject.layout.projectDirectory.dir("landing")
    val landingDist = landingDir.dir("dist")
    val webResources = layout.projectDirectory.dir("src/main/resources/web")

    register<Exec>("buildLandingNpm") {
        group = "landing"
        description = "Build the Vite landing site (npm)."
        workingDir = landingDir.asFile
        commandLine("bash", "-c", "npm ci || npm install; npm run build") // Node/npm required (dev machine)
        inputs.dir(landingDir.dir("src"))
        inputs.files(
            landingDir.file("index.html"),
            landingDir.file("privacy.html"),
            landingDir.file("terms.html"),
            landingDir.file("package.json"),
        )
        inputs.dir(landingDir.dir("public"))
        outputs.dir(landingDist)
    }

    register<Copy>("buildLanding") {
        group = "landing"
        description = "Build the landing site and copy it into resources/web (bundled by the jar)."
        dependsOn("buildLandingNpm")
        from(landingDist)
        into(webResources)
    }

    // --- Super-admin console (React + Vite + Tailwind) ---
    // Mirrors buildLanding: a MANUAL dev task (needs Node/npm) that builds console/ and copies its dist
    // into src/main/resources/console (served by the server at /console, bundled by the jar). NOT wired
    // into processResources — Heroku packages whatever console/ is already present.
    val consoleDir = rootProject.layout.projectDirectory.dir("console")
    val consoleDist = consoleDir.dir("dist")
    val consoleResources = layout.projectDirectory.dir("src/main/resources/console")

    register<Exec>("buildConsoleNpm") {
        group = "console"
        description = "Build the React console (npm)."
        workingDir = consoleDir.asFile
        commandLine("bash", "-c", "npm ci || npm install; npm run build")
        inputs.dir(consoleDir.dir("src"))
        inputs.files(consoleDir.file("index.html"), consoleDir.file("package.json"))
        outputs.dir(consoleDist)
    }

    register<Copy>("buildConsole") {
        group = "console"
        description = "Build the console and copy it into resources/console (bundled by the jar)."
        dependsOn("buildConsoleNpm")
        from(consoleDist)
        into(consoleResources)
    }
}
