plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.lint)
}

kotlin {
    android {
        namespace = "com.jyodroid.kunasismoayuda.core.media"
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

    val xcfName = "core:mediaKit"
    // Match the app's convention plugin (arm64 device + Apple-Silicon simulator).
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }

    sourceSets {
        // Platform-specific image pickers: each target has its own actual, so no shared
        // intermediate source set is needed — the default hierarchy wires the iOS leaves to
        // `iosMain` (where ImagePicker.ios.kt lives) automatically.
        commonMain.dependencies {
            implementation(compose.runtime) // @Composable expect fun rememberImagePicker
        }
        androidMain.dependencies {
            implementation(compose.ui)                      // LocalContext
            implementation(libs.androidx.activity.compose)  // rememberLauncherForActivityResult + contracts
        }
    }
}
