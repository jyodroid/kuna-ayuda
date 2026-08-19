import org.gradle.api.tasks.JavaExec
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.convention.cmp.application)
    alias(libs.plugins.compose.hot.reload)
}

kotlin {
    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.presentation)
            implementation(projects.core.data)
            implementation(projects.core.designsystem)
            implementation(projects.core.location)
            implementation(projects.core.media)
            implementation(projects.core.beacon)
            implementation(projects.feature.map)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
            implementation(libs.jetbrains.compose.viewmodel)
            implementation(libs.jetbrains.lifecycle.compose)
            implementation(libs.jetbrains.compose.navigation)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)

            // Network image loading (Lost & Found photos) — reuses the app's Ktor client.
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // DI
            implementation(libs.bundles.koin.common)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.jyodroid.kunasismoayuda.resources"
    generateResClass = always
}

// Release signing reads from a gitignored keystore.properties at the repo root (never committed). When
// it's absent (CI, a fresh clone, debug-only work) release signing is simply skipped, so nothing breaks.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = libs.versions.projectApplicationId.get()

    defaultConfig {
        applicationId = libs.versions.projectApplicationId.get()
        versionCode = libs.versions.projectVersionCode.get().toInt()
        versionName = libs.versions.projectVersionName.get()
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (keystorePropsFile.exists()) signingConfig = signingConfigs.getByName("release")
            // Minify is left OFF for now: R8 can strip reflection used by Koin/Ktor/kotlinx.serialization.
            // Turning it on later requires keep rules — a separate, tested change.
            isMinifyEnabled = false
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.jyodroid.kunasismoayuda.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            // User-facing app name (the .app bundle, Start-menu entry, DMG title).
            packageName = "Kuna Ayuda"
            packageVersion = libs.versions.desktopPackageVersion.get()
            description = "Disaster-relief resource network — earthquakes and wildfires first."
            vendor = "Kuna Ayuda"

            macOS {
                // Reverse-DNS bundle id — must be set explicitly since packageName now has a space.
                bundleID = libs.versions.projectApplicationId.get()
                iconFile.set(project.file("desktop-icons/icon.icns"))
            }
            windows {
                iconFile.set(project.file("desktop-icons/icon.ico"))
                menuGroup = "Kuna Ayuda"
            }
            linux {
                // Debian package names must be lowercase with no spaces.
                packageName = "kuna-ayuda"
                iconFile.set(project.file("desktop-icons/icon.png"))
            }
        }
    }
}

// Local-dev convenience: `:composeApp:run` (desktop) points the app at a local server via a JVM system
// property. Packaged/release builds (packageDmg/Msi/Deb, the runnable jar) never get this flag, so they
// use the production API — a release can't accidentally ship pointing at a developer's machine.
tasks.matching { it.name == "run" }.configureEach {
    (this as? JavaExec)?.systemProperty("kuna.local", "true")
}
