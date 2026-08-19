package com.jyodroid.kunasismoayuda.core.data.remote

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import platform.Foundation.NSProcessInfo

// iOS: production by default. A *debug* framework (what Xcode builds when you Run) uses the simulator's
// localhost; a release framework (App Store / TestFlight) always uses prod. A KUNA_SERVER_URL env var in
// the Xcode scheme overrides it (handy for debugging on a real device against a machine on your network).
@OptIn(ExperimentalNativeApi::class)
actual fun defaultServerBaseUrl(): String {
    val override = NSProcessInfo.processInfo.environment["KUNA_SERVER_URL"] as? String
    return when {
        !override.isNullOrBlank() -> override
        Platform.isDebugBinary -> "http://localhost:8080"
        else -> PROD_BASE_URL
    }
}
