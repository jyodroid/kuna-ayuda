package com.jyodroid.kunasismoayuda.core.data.offline

import com.jyodroid.kunasismoayuda.core.data.remote.SosApi
import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import com.jyodroid.kunasismoayuda.core.domain.model.SosSendResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Raised when the server permanently rejects a report (4xx) so it will never succeed on retry. */
class SosRejectedException(val statusCode: Int) :
    Exception("Server rejected SOS report with status $statusCode")

/**
 * Offline-first delivery queue for SOS/SAFE reports. Every report is persisted before any network
 * call, so a report is never lost — not to a dead network, not to the app being killed. Delivery is
 * attempted immediately; if the network is down the report stays queued and a background loop keeps
 * retrying with exponential backoff until it gets through.
 */
@OptIn(ExperimentalTime::class)
class SosOutbox(
    private val api: SosApi,
    private val store: SosOutboxStore,
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val _pending = MutableStateFlow(0)
    val pending: StateFlow<Int> = _pending.asStateFlow()

    private var retryJob: Job? = null
    private var started = false

    private enum class Outcome { DELIVERED, REJECTED, RETRY }

    /** Load anything left over from a previous session and resume retrying. Idempotent. */
    fun start() {
        if (started) return
        started = true
        scope.launch {
            _pending.value = store.read().size
            if (_pending.value > 0) scheduleRetry()
        }
    }

    /**
     * Persist [sos], then try to deliver it right away.
     * @return [SosSendResult.SENT] if it reached the server, [SosSendResult.QUEUED] if it was
     *   stored for automatic retry.
     * @throws SosRejectedException if the server refused the report (bad data — retrying won't help).
     */
    suspend fun enqueue(sos: NewSos): SosSendResult {
        val item = QueuedSos.from(
            id = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(1_000_000)}",
            createdAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            sos = sos,
        )
        persist(item)
        return when (attemptOnce(item)) {
            Outcome.DELIVERED -> SosSendResult.SENT
            Outcome.REJECTED -> throw SosRejectedException(statusCode = 400)
            Outcome.RETRY -> {
                scheduleRetry()
                SosSendResult.QUEUED
            }
        }
    }

    /** Try flushing the queue immediately (e.g. when the user reopens the SOS screen). */
    fun flush() {
        scope.launch {
            flushOnce()
            if (_pending.value > 0) scheduleRetry()
        }
    }

    private suspend fun persist(item: QueuedSos) = mutex.withLock {
        val items = store.read() + item
        store.write(items)
        _pending.value = items.size
    }

    private suspend fun remove(id: String) = mutex.withLock {
        val items = store.read().filterNot { it.id == id }
        store.write(items)
        _pending.value = items.size
    }

    private suspend fun attemptOnce(item: QueuedSos): Outcome {
        val outcome = try {
            when (val status = api.send(item.toRequestDto())) {
                in 200..299 -> Outcome.DELIVERED
                in 400..499 -> Outcome.REJECTED
                else -> Outcome.RETRY
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Throwable) {
            // Network error / host unreachable: transient, keep it queued.
            Outcome.RETRY
        }
        if (outcome != Outcome.RETRY) remove(item.id)
        return outcome
    }

    private fun scheduleRetry() {
        if (retryJob?.isActive == true) return
        retryJob = scope.launch {
            var delayMs = INITIAL_DELAY_MS
            while (isActive && _pending.value > 0) {
                delay(delayMs)
                flushOnce()
                delayMs = (delayMs * 2).coerceAtMost(MAX_DELAY_MS)
            }
        }
    }

    internal suspend fun flushOnce() {
        val items = mutex.withLock { store.read() }
        for (item in items) {
            // Stop on the first transient failure — still offline, wait for the next cycle.
            if (attemptOnce(item) == Outcome.RETRY) return
        }
    }

    private companion object {
        const val INITIAL_DELAY_MS = 15_000L
        const val MAX_DELAY_MS = 300_000L // cap backoff at 5 minutes
    }
}
