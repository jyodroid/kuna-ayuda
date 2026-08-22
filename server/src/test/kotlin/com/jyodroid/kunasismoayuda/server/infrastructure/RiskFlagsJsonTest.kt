package com.jyodroid.kunasismoayuda.server.infrastructure

import com.jyodroid.kunasismoayuda.server.ai.ClassifiedPost
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RiskFlagsJsonTest {

    @Test
    fun round_trips_a_list_of_flags() {
        val flags = listOf("ASKS_FOR_MONEY", "NO_SOURCE")
        assertEquals(flags, RiskFlagsJson.decode(RiskFlagsJson.encode(flags)))
    }

    @Test
    fun null_blank_and_garbage_decode_to_empty() {
        assertTrue(RiskFlagsJson.decode(null).isEmpty())
        assertTrue(RiskFlagsJson.decode("").isEmpty())
        assertTrue(RiskFlagsJson.decode("not json").isEmpty())
        assertEquals("[]", RiskFlagsJson.encode(emptyList()))
    }

    @Test
    fun classified_post_parses_risk_flags_from_the_model_json() {
        // Mirrors the JSON the model returns under the structured-output schema (text OR vision path).
        val json = """
            {"kind":"REQUEST","resourceType":"OTHER","region":"Bogotá","description":"Envíen dinero a esta cuenta",
             "contactPhone":"","contactName":"","collectionPoints":[],
             "riskFlags":["ASKS_FOR_MONEY","NO_SOURCE"]}
        """.trimIndent()
        val post = Json { ignoreUnknownKeys = true }.decodeFromString<ClassifiedPost>(json)
        assertEquals(listOf("ASKS_FOR_MONEY", "NO_SOURCE"), post.riskFlags)
    }
}
