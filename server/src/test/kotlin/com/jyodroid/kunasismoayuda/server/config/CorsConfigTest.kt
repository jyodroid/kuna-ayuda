package com.jyodroid.kunasismoayuda.server.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CorsConfigTest {

    @Test
    fun strips_the_https_scheme_from_the_origin() {
        // The production crash: ALLOWED_ORIGIN carried a scheme, which allowHost() rejects.
        val rules = parseAllowedOrigins("https://kuna-ayuda.herokuapp.com")
        assertEquals(listOf(OriginRule("kuna-ayuda.herokuapp.com", "https")), rules)
    }

    @Test
    fun keeps_http_scheme_when_explicit() {
        val rules = parseAllowedOrigins("http://localhost:5173")
        assertEquals(listOf(OriginRule("localhost:5173", "http")), rules)
    }

    @Test
    fun drops_a_trailing_path_or_slash() {
        assertEquals("example.org", parseAllowedOrigins("https://example.org/").single().host)
        assertEquals("example.org", parseAllowedOrigins("https://example.org/some/path").single().host)
    }

    @Test
    fun bare_host_defaults_to_https() {
        assertEquals(listOf(OriginRule("example.org", "https")), parseAllowedOrigins("example.org"))
    }

    @Test
    fun supports_a_comma_separated_list() {
        val rules = parseAllowedOrigins("https://kunaayuda.org, https://kuna-ayuda.herokuapp.com")
        assertEquals(
            listOf(
                OriginRule("kunaayuda.org", "https"),
                OriginRule("kuna-ayuda.herokuapp.com", "https"),
            ),
            rules,
        )
    }

    @Test
    fun blank_or_null_yields_no_rules_so_dev_falls_back_to_anyHost() {
        assertTrue(parseAllowedOrigins(null).isEmpty())
        assertTrue(parseAllowedOrigins("   ").isEmpty())
        assertTrue(parseAllowedOrigins(" , ").isEmpty())
    }
}
