package com.jyodroid.kunasismoayuda.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The shared client's auto-logout hook: a 401 on a request that carried a Bearer token fires
 * [installKunaDefaults]'s `onUnauthorized`; a 401 on an anonymous request (or any success) does not.
 */
class HttpClientAuthTest {

    private fun client(status: HttpStatusCode, onUnauthorized: () -> Unit): HttpClient =
        HttpClient(MockEngine { respond("", status) }) { installKunaDefaults(onUnauthorized) }

    @Test
    fun `401 on a token-bearing request triggers onUnauthorized`() = runTest {
        var loggedOut = false
        client(HttpStatusCode.Unauthorized) { loggedOut = true }
            .get("https://example.test/api/board/pending") { bearerAuth("expired-jwt") }
        assertTrue(loggedOut, "expired token 401 should log the moderator out")
    }

    @Test
    fun `401 without a token does not trigger onUnauthorized`() = runTest {
        var loggedOut = false
        client(HttpStatusCode.Unauthorized) { loggedOut = true }
            .get("https://example.test/api/auth/login") // no Authorization header (e.g. bad credentials)
        assertFalse(loggedOut, "an anonymous 401 must not clear a session")
    }

    @Test
    fun `a successful token-bearing request does not trigger onUnauthorized`() = runTest {
        var loggedOut = false
        client(HttpStatusCode.OK) { loggedOut = true }
            .get("https://example.test/api/board/pending") { bearerAuth("valid-jwt") }
        assertFalse(loggedOut, "a 200 must not log the moderator out")
    }
}
