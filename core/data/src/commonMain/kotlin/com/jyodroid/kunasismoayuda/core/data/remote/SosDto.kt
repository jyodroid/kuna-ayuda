package com.jyodroid.kunasismoayuda.core.data.remote

import com.jyodroid.kunasismoayuda.core.domain.model.SafeCheckIn
import com.jyodroid.kunasismoayuda.core.domain.model.SosReport
import com.jyodroid.kunasismoayuda.core.domain.model.SosStats
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import kotlinx.serialization.Serializable

@Serializable
data class SosRequestDto(
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val region: String? = null,
    val message: String? = null,
    val contactPhone: String? = null,
    val displayName: String? = null,
    val country: String = "CO",
)

/** A public "I'm safe" check-in (`GET /api/sos/safe`). Name + region + time only. */
@Serializable
data class SafeCheckInDto(
    val id: Int,
    val name: String,
    val region: String? = null,
    val createdAtEpochMs: Long,
)

fun SafeCheckInDto.toDomain(): SafeCheckIn = SafeCheckIn(
    id = id,
    name = name,
    region = region,
    createdAtEpochMs = createdAtEpochMs,
)

/** A report returned by the responder endpoint (`GET /api/sos`). Mirrors the server's SosResponse. */
@Serializable
data class SosResponseDto(
    val id: Int,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val region: String? = null,
    val message: String? = null,
    val contactPhone: String? = null,
    val createdAt: String,
    val handledAt: String? = null,
    val handledBy: String? = null,
)

fun SosResponseDto.toDomain(): SosReport = SosReport(
    id = id,
    // Anything the server didn't tag SAFE is treated as an SOS (fail toward "needs help").
    status = if (status.equals("SAFE", ignoreCase = true)) SosStatus.SAFE else SosStatus.SOS,
    latitude = latitude,
    longitude = longitude,
    region = region,
    message = message,
    contactPhone = contactPhone,
    createdAt = createdAt,
    handledAt = handledAt,
    handledBy = handledBy,
)

/** Responder dashboard counts (`GET /api/sos/stats`). */
@Serializable
data class SosStatsDto(
    val pendingSos: Int,
    val pendingSafe: Int,
    val handledSos: Int,
    val handledSafe: Int,
)

fun SosStatsDto.toDomain(): SosStats = SosStats(
    pendingSos = pendingSos,
    pendingSafe = pendingSafe,
    handledSos = handledSos,
    handledSafe = handledSafe,
)
