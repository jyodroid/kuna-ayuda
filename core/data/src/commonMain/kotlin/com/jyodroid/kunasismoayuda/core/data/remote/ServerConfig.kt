package com.jyodroid.kunasismoayuda.core.data.remote

/**
 * Base URL of the Kuna backend for local development. Differs per platform because
 * "localhost" means different things on an Android emulator vs. desktop/iOS simulator.
 */
expect fun defaultServerBaseUrl(): String

/**
 * Shared "our apps only" key. Sent as the `X-App-Key` header on every API request (via
 * [HttpClientFactory]); the server enforces it when its `APP_CLIENT_KEY` env is set to this same value
 * (see server `config/AppGate.kt`). Same across platforms, so a plain constant.
 *
 * **Deterrent, not a secret:** it ships inside the app binary and can be extracted, so it deters
 * bots/scanners/browser callers but not a determined reverse-engineer. Rotate by changing this value
 * and the server env together. Real "only our app" enforcement (attestation) is a separate, deferred task.
 */
const val APP_CLIENT_KEY: String = "b7e2d40915a86c3f0e1d7942bc63f58a2049e1cd76b8340af5921e6d0c8b47f3"

/**
 * Public website that hosts the Privacy Policy and Terms (the same Ktor server serves them at
 * `/privacy` and `/terms`). The app links here from the Guide screen, and the app-store listings point
 * their required Privacy-Policy URL here too.
 *
 * TODO: set to the real registered domain (e.g. https://kunaayuda.org) before launch.
 */
const val SITE_BASE_URL: String = "https://kunaayuda.org"
const val PRIVACY_URL: String = "$SITE_BASE_URL/privacy"
const val TERMS_URL: String = "$SITE_BASE_URL/terms"
