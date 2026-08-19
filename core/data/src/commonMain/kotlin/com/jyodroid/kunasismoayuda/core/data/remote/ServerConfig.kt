package com.jyodroid.kunasismoayuda.core.data.remote

/**
 * Production API base URL (the Heroku server). **Release/packaged apps use this** — the whole point is
 * that a downloaded app talks to the live backend, not a developer's machine. For LOCAL development
 * against a server on your own machine, override it per platform (see [defaultServerBaseUrl]).
 *
 * TODO: switch to the custom domain (e.g. https://api.kunaayuda.org) once DNS is set up.
 */
const val PROD_BASE_URL: String = "https://kuna-ayuda-def81359e2e0.herokuapp.com"

/**
 * Base URL of the Kuna backend, chosen automatically by **build type** so a release can never ship
 * pointing at a developer's machine:
 *  - **Release / packaged builds → [PROD_BASE_URL]** (the safe default on every platform).
 *  - **Debug / local dev → a local server.** Desktop: `:composeApp:run` sets `-Dkuna.local=true`
 *    (or pass it yourself) → `http://localhost:8080`. Android: a *debuggable* build → `http://10.0.2.2:8080`
 *    (emulator loopback). iOS: a *debug* framework → `http://localhost:8080` (simulator).
 *
 * Explicit overrides still win where handy: `-Dkuna.server.url=...` on desktop, `KUNA_SERVER_URL` env on iOS.
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
 * TODO: set to the real registered domain (e.g. https://kunaayuda.org) once DNS is set up. For now it
 * points at the live Heroku server, which serves /privacy and /terms, so the in-app links work today.
 */
const val SITE_BASE_URL: String = PROD_BASE_URL
const val PRIVACY_URL: String = "$SITE_BASE_URL/privacy"
const val TERMS_URL: String = "$SITE_BASE_URL/terms"
