package com.jyodroid.kunasismoayuda.server.upstream

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class FirmsSourceTest {

    private val bbox = BBox(minLat = -5.0, maxLat = 13.0, minLon = -82.0, maxLon = -66.0)

    private fun sourceReturning(body: String, status: HttpStatusCode = HttpStatusCode.OK): FirmsSource {
        val engine = MockEngine {
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.CSV.toString()),
            )
        }
        return FirmsSource(HttpClient(engine), mapKey = "test-key")
    }

    @Test
    fun disabled_without_a_map_key() = runBlocking {
        val engine = MockEngine { respond("", HttpStatusCode.OK) }
        val source = FirmsSource(HttpClient(engine), mapKey = "  ")
        assertTrue(source.recentFires(bbox).isEmpty())
        assertEquals(false, source.isEnabled)
    }

    @Test
    fun parses_rows_and_resolves_columns_by_header_name() = runBlocking {
        val csv = """
            latitude,longitude,bright_ti4,scan,track,acq_date,acq_time,satellite,instrument,confidence,version,bright_ti5,frp,daynight
            3.5,-75.2,330.1,0.4,0.4,2026-08-17,1830,N,VIIRS,high,2.0,290.0,42.7,D
            4.1,-73.9,310.0,0.4,0.4,2026-08-17,0905,N,VIIRS,nominal,2.0,280.0,5.3,N
        """.trimIndent()
        val fires = sourceReturning(csv).recentFires(bbox)
        assertEquals(2, fires.size)
        val first = fires.first()
        assertEquals(3.5, first.latitude)
        assertEquals(-75.2, first.longitude)
        assertEquals(42.7, first.frpMw)
        assertEquals(330.1, first.brightnessK) // from bright_ti4
        assertEquals("high", first.confidence)
        assertEquals("D", first.daynight)
        assertNull(first.place) // FIRMS points carry no place label
    }

    @Test
    fun tolerates_a_different_column_order() = runBlocking {
        // Columns reordered vs. the canonical header — must still resolve by name, not index.
        val csv = """
            frp,acq_time,longitude,latitude,acq_date,confidence,satellite
            99.9,1200,-74.0,4.0,2026-08-17,high,N
        """.trimIndent()
        val fire = sourceReturning(csv).recentFires(bbox).single()
        assertEquals(4.0, fire.latitude)
        assertEquals(-74.0, fire.longitude)
        assertEquals(99.9, fire.frpMw)
    }

    @Test
    fun computes_utc_epoch_from_acq_date_and_time() = runBlocking {
        val csv = """
            latitude,longitude,acq_date,acq_time,frp
            1.0,-70.0,2026-08-17,0830,10.0
        """.trimIndent()
        val fire = sourceReturning(csv).recentFires(bbox).single()
        val expected = LocalDateTime.of(2026, 8, 17, 8, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals(expected, fire.time)
    }

    @Test
    fun skips_rows_with_unparseable_coordinates() = runBlocking {
        val csv = """
            latitude,longitude,acq_date,acq_time,frp
            not-a-number,-70.0,2026-08-17,0830,10.0
            2.0,-71.0,2026-08-17,0830,11.0
        """.trimIndent()
        val fires = sourceReturning(csv).recentFires(bbox)
        assertEquals(1, fires.size)
        assertEquals(2.0, fires.single().latitude)
    }

    @Test
    fun returns_empty_on_a_non_csv_quota_message() = runBlocking {
        // FIRMS returns a plain-text error/quota notice (no CSV header) instead of rows.
        val body = "Invalid MAP_KEY or you have exceeded your transaction limit."
        assertTrue(sourceReturning(body).recentFires(bbox).isEmpty())
    }

    @Test
    fun returns_empty_when_the_request_fails() = runBlocking {
        val source = sourceReturning("boom", status = HttpStatusCode.InternalServerError)
        // A non-2xx still yields a body; the header guard rejects it as non-CSV → empty, no throw.
        assertTrue(source.recentFires(bbox).isEmpty())
    }
}
