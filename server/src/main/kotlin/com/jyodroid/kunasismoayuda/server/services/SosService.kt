package com.jyodroid.kunasismoayuda.server.services

import com.jyodroid.kunasismoayuda.server.domain.models.NewSosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SafeCheckIn
import com.jyodroid.kunasismoayuda.server.domain.models.SosReport
import com.jyodroid.kunasismoayuda.server.domain.models.SosStats
import com.jyodroid.kunasismoayuda.server.routes.dto.SafeCheckInResponse
import com.jyodroid.kunasismoayuda.server.domain.repositories.SosRepository
import com.jyodroid.kunasismoayuda.server.routes.dto.SosRequest
import com.jyodroid.kunasismoayuda.server.routes.dto.SosResponse
import com.jyodroid.kunasismoayuda.server.routes.dto.SosStatsResponse

class SosService(private val repository: SosRepository) {

    companion object {
        val STATUSES = setOf("SOS", "SAFE")
        const val DEFAULT_COUNTRY = "CO"
        const val SAFE_WINDOW_DAYS = 14L
        const val SAFE_LIMIT = 200
    }

    fun create(request: SosRequest): SosResponse = repository.create(
        NewSosReport(
            status = request.status.uppercase(),
            latitude = request.latitude,
            longitude = request.longitude,
            region = request.region?.trim()?.ifBlank { null },
            message = request.message?.trim()?.ifBlank { null },
            contactPhone = request.contactPhone?.trim()?.ifBlank { null },
            displayName = request.displayName?.trim()?.take(120)?.ifBlank { null },
            country = request.country?.trim()?.uppercase()?.take(2)?.ifBlank { null } ?: DEFAULT_COUNTRY,
        ),
    ).toResponse()

    /** Public reassurance list of named SAFE check-ins for [country] (newest first). */
    fun listPublicSafe(country: String): List<SafeCheckInResponse> =
        repository.listPublicSafe(
            country = country.trim().uppercase().take(2).ifBlank { DEFAULT_COUNTRY },
            sinceDays = SAFE_WINDOW_DAYS,
            limit = SAFE_LIMIT,
        ).map { it.toSafeResponse() }

    /**
     * Responder view. [status] filters by "SOS" or "SAFE" (case-insensitive); "ALL"/null returns both.
     * An unrecognized value returns both rather than erroring (a responder should never get an empty
     * screen from a bad query param). [archived] false = pending only, true = archived only, null = both.
     */
    fun list(status: String?, archived: Boolean?): List<SosResponse> {
        val normalized = status?.uppercase()?.takeIf { it in STATUSES }
        return repository.list(normalized, archived).map { it.toResponse() }
    }

    /** The raw domain report by id (for the audit before-snapshot); null if absent. */
    fun find(id: Int): SosReport? = repository.find(id)

    /** Archive a report as attended/notified. Returns false if the id doesn't exist. */
    fun markHandled(id: Int, by: String?): Boolean = repository.markHandled(id, by)

    /** Restore an archived report to the active list. Returns false if the id doesn't exist. */
    fun reopen(id: Int): Boolean = repository.reopen(id)

    /** Permanently delete a report. Returns false if the id doesn't exist. */
    fun delete(id: Int): Boolean = repository.delete(id)

    fun stats(): SosStatsResponse = repository.stats().toResponse()

    private fun SosStats.toResponse() = SosStatsResponse(
        pendingSos = pendingSos,
        pendingSafe = pendingSafe,
        handledSos = handledSos,
        handledSafe = handledSafe,
    )

    private fun SosReport.toResponse() = SosResponse(
        id = id,
        status = status,
        latitude = latitude,
        longitude = longitude,
        region = region,
        message = message,
        contactPhone = contactPhone,
        displayName = displayName,
        createdAt = createdAt.toString(),
        handledAt = handledAt?.toString(),
        handledBy = handledBy,
    )

    private fun SafeCheckIn.toSafeResponse() = SafeCheckInResponse(
        id = id,
        name = displayName,
        region = region,
        createdAtEpochMs = createdAtEpochMs,
    )
}
