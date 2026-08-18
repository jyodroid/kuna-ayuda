package com.jyodroid.kunasismoayuda.core.domain.repository

import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import com.jyodroid.kunasismoayuda.core.domain.model.SosReport
import com.jyodroid.kunasismoayuda.core.domain.model.SosSendResult
import com.jyodroid.kunasismoayuda.core.domain.model.SosStats
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Sends SOS alerts and "I'm safe" check-ins to the backend (`POST /api/sos`).
 *
 * Delivery is offline-first: [send] persists the report and attempts immediate delivery, returning
 * [SosSendResult.SENT] if it reached the server or [SosSendResult.QUEUED] if it was stored to be
 * retried automatically once connectivity returns. [pending] exposes how many reports are still
 * waiting so the UI can reassure the user their SOS is not lost.
 */
interface SosRepository {
    val pending: StateFlow<Int>

    suspend fun send(sos: NewSos): SosSendResult

    /** Resume retrying any reports left over from a previous session. Call once at app start. */
    fun start()

    /**
     * Responder view (moderator-only, requires an active session). Lists submitted reports newest
     * first; [status] filters by SOS or SAFE, null returns both. [archived] false = pending only,
     * true = archived (handled) only, null = both.
     */
    suspend fun listActive(status: SosStatus?, archived: Boolean?): List<SosReport>

    /** Archive a report as attended/notified (moderator-only). */
    suspend fun markHandled(id: Int)

    /** Restore an archived report to the active list (moderator-only). */
    suspend fun reopen(id: Int)

    /** Permanently delete a report (moderator-only). */
    suspend fun delete(id: Int)

    /** Pending-vs-handled counts for the responder dashboard (moderator-only). */
    suspend fun stats(): SosStats
}
