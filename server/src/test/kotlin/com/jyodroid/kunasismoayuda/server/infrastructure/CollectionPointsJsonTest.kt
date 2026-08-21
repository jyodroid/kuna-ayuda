package com.jyodroid.kunasismoayuda.server.infrastructure

import com.jyodroid.kunasismoayuda.server.ai.ClassifiedPost
import com.jyodroid.kunasismoayuda.server.domain.models.CollectionPoint
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CollectionPointsJsonTest {

    @Test
    fun round_trips_a_list_of_points() {
        val points = listOf(
            CollectionPoint("Parroquia San José", "Cra 5 #10-20", "8am-5pm"),
            CollectionPoint("Colegio Central", "", ""),
        )
        val decoded = CollectionPointsJson.decode(CollectionPointsJson.encode(points))
        assertEquals(points, decoded)
    }

    @Test
    fun null_blank_and_garbage_decode_to_empty() {
        assertTrue(CollectionPointsJson.decode(null).isEmpty())
        assertTrue(CollectionPointsJson.decode("").isEmpty())
        assertTrue(CollectionPointsJson.decode("not json").isEmpty())
        assertEquals("[]", CollectionPointsJson.encode(emptyList()))
    }

    @Test
    fun classified_post_parses_collection_points_from_the_model_json() {
        // Mirrors the JSON the model returns under the structured-output schema.
        val json = """
            {"kind":"REQUEST","resourceType":"WATER","region":"Bogotá","description":"Necesitamos agua",
             "contactPhone":"","contactName":"",
             "collectionPoints":[{"name":"Parroquia San José","address":"Cra 5 #10-20","hours":"8am-5pm"}]}
        """.trimIndent()
        val post = Json { ignoreUnknownKeys = true }.decodeFromString<ClassifiedPost>(json)
        assertEquals(1, post.collectionPoints.size)
        assertEquals("Parroquia San José", post.collectionPoints.first().name)
    }
}
