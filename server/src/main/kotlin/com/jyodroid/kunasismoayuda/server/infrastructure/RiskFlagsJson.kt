package com.jyodroid.kunasismoayuda.server.infrastructure

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** JSON (de)serialization for the `risk_flags` TEXT columns (resource_posts + classify_cache). */
object RiskFlagsJson {
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(String.serializer())

    /** Encode a list to a JSON array string; empty list ⇒ `"[]"`. */
    fun encode(flags: List<String>): String = json.encodeToString(serializer, flags)

    /** Decode a stored JSON array (null/blank/garbage ⇒ empty list). */
    fun decode(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
    }
}
