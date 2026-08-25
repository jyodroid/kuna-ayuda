package com.jyodroid.kunasismoayuda.core.data.repository

import com.jyodroid.kunasismoayuda.core.data.auth.SessionManager
import com.jyodroid.kunasismoayuda.core.data.offline.SosOutbox
import com.jyodroid.kunasismoayuda.core.data.remote.SosApi
import com.jyodroid.kunasismoayuda.core.data.remote.toDomain
import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import com.jyodroid.kunasismoayuda.core.domain.model.SafeCheckIn
import com.jyodroid.kunasismoayuda.core.domain.model.SosReport
import com.jyodroid.kunasismoayuda.core.domain.model.SosSendResult
import com.jyodroid.kunasismoayuda.core.domain.model.SosStats
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Offline-first SOS delivery. Submission persistence/retry lives in [SosOutbox]; the responder read
 * ([listActive]) goes straight through [SosApi] with the moderator token from [SessionManager].
 */
class SosRepositoryImpl(
    private val outbox: SosOutbox,
    private val api: SosApi,
    private val sessionManager: SessionManager,
) : SosRepository {

    override val pending: StateFlow<Int> = outbox.pending

    override suspend fun send(sos: NewSos): SosSendResult = outbox.enqueue(sos)

    override fun start() = outbox.start()

    override suspend fun listPublicSafe(country: String): List<SafeCheckIn> =
        api.listPublicSafe(country).map { it.toDomain() }

    override suspend fun listActive(status: SosStatus?, archived: Boolean?): List<SosReport> =
        api.listActive(status?.name, archived, sessionManager.requireToken()).map { it.toDomain() }

    override suspend fun markHandled(id: Int) = api.markHandled(id, sessionManager.requireToken())

    override suspend fun reopen(id: Int) = api.reopen(id, sessionManager.requireToken())

    override suspend fun delete(id: Int) = api.delete(id, sessionManager.requireToken())

    override suspend fun stats(): SosStats = api.stats(sessionManager.requireToken()).toDomain()
}
