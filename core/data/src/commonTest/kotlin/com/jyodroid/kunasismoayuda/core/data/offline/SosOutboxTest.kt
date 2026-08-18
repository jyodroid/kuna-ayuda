package com.jyodroid.kunasismoayuda.core.data.offline

import com.jyodroid.kunasismoayuda.core.data.remote.SosApi
import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import com.jyodroid.kunasismoayuda.core.domain.model.SosSendResult
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SosOutboxTest {

    private enum class Mode { OFFLINE, OK, REJECT }

    /** A [SosApi] whose behaviour is switched at runtime via [mode] (simulates losing/regaining signal). */
    private fun api(mode: () -> Mode): SosApi {
        val engine = MockEngine {
            when (mode()) {
                Mode.OFFLINE -> throw RuntimeException("no connectivity")
                Mode.OK -> respond("", HttpStatusCode.Created)
                Mode.REJECT -> respond("", HttpStatusCode.BadRequest)
            }
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        return SosApi(client, baseUrl = "http://test")
    }

    private val sampleSos = NewSos(
        status = SosStatus.SOS,
        latitude = 4.6,
        longitude = -74.1,
        region = "Bogotá",
        message = "atrapado",
        contactPhone = "123",
    )

    private fun store(fs: FakeFileSystem = FakeFileSystem()) =
        SosOutboxStore(fileSystem = fs, path = "/outbox/sos.json".toPath())

    @Test
    fun queues_when_offline() = runTest {
        val outbox = SosOutbox(api { Mode.OFFLINE }, store(), backgroundScope)
        assertEquals(SosSendResult.QUEUED, outbox.enqueue(sampleSos))
        assertEquals(1, outbox.pending.value)
    }

    @Test
    fun delivers_immediately_when_online() = runTest {
        val outbox = SosOutbox(api { Mode.OK }, store(), backgroundScope)
        assertEquals(SosSendResult.SENT, outbox.enqueue(sampleSos))
        assertEquals(0, outbox.pending.value)
    }

    @Test
    fun drops_report_on_permanent_rejection() = runTest {
        val outbox = SosOutbox(api { Mode.REJECT }, store(), backgroundScope)
        assertFailsWith<SosRejectedException> { outbox.enqueue(sampleSos) }
        assertEquals(0, outbox.pending.value)
    }

    @Test
    fun retries_and_delivers_after_reconnect() = runTest {
        var mode = Mode.OFFLINE
        val outbox = SosOutbox(api { mode }, store(), backgroundScope)
        assertEquals(SosSendResult.QUEUED, outbox.enqueue(sampleSos))
        assertEquals(1, outbox.pending.value)

        mode = Mode.OK
        outbox.flushOnce()
        assertEquals(0, outbox.pending.value)
    }

    @Test
    fun persisted_report_survives_restart_and_delivers() = runTest {
        val fs = FakeFileSystem()
        val path = "/outbox/sos.json".toPath()

        // First session: offline, so the report is written to disk and kept.
        val first = SosOutbox(api { Mode.OFFLINE }, SosOutboxStore(fs, path), backgroundScope)
        first.enqueue(sampleSos)
        assertEquals(1, SosOutboxStore(fs, path).read().size)

        // Fresh process (new store over the same file), now with connectivity.
        val restarted = SosOutbox(api { Mode.OK }, SosOutboxStore(fs, path), backgroundScope)
        restarted.flushOnce()
        assertEquals(0, restarted.pending.value)
        assertEquals(0, SosOutboxStore(fs, path).read().size)
    }
}
