plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.lint)
}

kotlin {
    android {
        namespace = "com.jyodroid.kunasismoayuda.feature.map"
        compileSdk {
            version = release(36)
        }
        minSdk = 26

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm()

    val xcfName = "feature:mapKit"
    // Match the app's convention plugin (arm64 device + Apple-Silicon simulator); the MapLibre
    // Compose runtime doesn't publish an iosX64 (Intel simulator) variant.
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }

    sourceSets {
        // Shared Android + iOS implementation of QuakeMap (MapLibre-backed).
        // Desktop (jvm) intentionally uses a separate placeholder actual so we don't pull in
        // MapLibre's desktop runtime (which requires Java 25) in this milestone.
        val mobileMain = create("mobileMain").apply {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.maplibre.compose)
            }
        }
        androidMain { dependsOn(mobileMain) }
        // Wire the iOS *leaf* source sets directly to mobileMain. Adding manual dependsOn edges
        // disables the default hierarchy template, which otherwise connects the leaves to the
        // intermediate `iosMain` — so routing through `iosMain { dependsOn(mobileMain) }` silently
        // leaves the leaves (and thus the QuakeMap actual + MapLibre dep) out of the iOS compilation.
        getByName("iosArm64Main") { dependsOn(mobileMain) }
        getByName("iosSimulatorArm64Main") { dependsOn(mobileMain) }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(projects.core.domain)
        }
    }
}
