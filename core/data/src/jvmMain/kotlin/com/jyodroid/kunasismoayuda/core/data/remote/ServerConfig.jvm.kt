package com.jyodroid.kunasismoayuda.core.data.remote

// Desktop: production by default (so a packaged app can never point at localhost). `:composeApp:run`
// sets -Dkuna.local=true for local dev; -Dkuna.server.url=<url> overrides with an explicit host.
actual fun defaultServerBaseUrl(): String {
    val explicit = System.getProperty("kuna.server.url")
    return when {
        !explicit.isNullOrBlank() -> explicit
        System.getProperty("kuna.local") == "true" -> "http://localhost:8080"
        else -> PROD_BASE_URL
    }
}
