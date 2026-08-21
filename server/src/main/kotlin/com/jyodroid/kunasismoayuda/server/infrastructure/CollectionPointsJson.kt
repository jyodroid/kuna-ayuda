package com.jyodroid.kunasismoayuda.server.infrastructure

import com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** JSON (de)serialization for the `collection_points` TEXT columns (resource_posts + classify_cache). */
object CollectionPointsJson {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val serializer = ListSerializer(CollectionPoint.serializer())

    /** Encode a list to a JSON array string; empty list ⇒ `"[]"`. */
    fun encode(points: List<CollectionPoint>): String = json.encodeToString(serializer, points)

    /** Decode a stored JSON array (null/blank/garbage ⇒ empty list). */
    fun decode(raw: String?): List<CollectionPoint> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }.getOrDefault(emptyList())
    }
}
