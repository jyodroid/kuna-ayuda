package com.jyodroid.kunasismoayuda.core.data.remote

// Android: production by default. For local emulator dev against a server on your machine, temporarily
// return "http://10.0.2.2:8080" (the emulator's loopback to the host) instead of PROD_BASE_URL.
actual fun defaultServerBaseUrl(): String = PROD_BASE_URL
