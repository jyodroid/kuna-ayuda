package com.jyodroid.kunasismoayuda.core.data.remote

import platform.Foundation.NSProcessInfo

// iOS: production by default; set a KUNA_SERVER_URL env var in the Xcode scheme for local dev.
actual fun defaultServerBaseUrl(): String {
    val override = NSProcessInfo.processInfo.environment["KUNA_SERVER_URL"] as? String
    return override?.takeIf { it.isNotBlank() } ?: PROD_BASE_URL
}
