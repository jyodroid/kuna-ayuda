package com.jyodroid.kunasismoayuda.core.data.remote

// Desktop: production by default; run with `-Dkuna.server.url=http://localhost:8080` for local dev.
actual fun defaultServerBaseUrl(): String =
    System.getProperty("kuna.server.url")?.takeIf { it.isNotBlank() } ?: PROD_BASE_URL
