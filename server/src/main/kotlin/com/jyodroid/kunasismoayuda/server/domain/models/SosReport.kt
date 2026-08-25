package com.jyodroid.kunasismoayuda.server.domain.models

import java.time.LocalDateTime

/** A geolocated SOS ("need help") or an "I'm safe" check-in. */
data class SosReport(
    val id: Int,
    val status: String,          // SOS | SAFE
    val latitude: Double?,
    val longitude: Double?,
    val region: String?,
    val message: String?,
    val contactPhone: String?,
    // Public name on a SAFE check-in; null for SOS and legacy rows.
    val displayName: String? = null,
    val country: String = "CO",
    val createdAt: LocalDateTime,
    // Responder lifecycle: null = still pending/active; a timestamp = archived (attended/notified).
    val handledAt: LocalDateTime? = null,
    val handledBy: String? = null,
)

data class NewSosReport(
    val status: String,
    val latitude: Double?,
    val longitude: Double?,
    val region: String?,
    val message: String?,
    val contactPhone: String?,
    val displayName: String?,
    val country: String,
)

/**
 * A public "I'm safe" check-in, as shown on the community reassurance list. Deliberately minimal —
 * name + region + time only; never coordinates, phone, or message.
 */
data class SafeCheckIn(
    val id: Int,
    val displayName: String,
    val region: String?,
    val createdAtEpochMs: Long,
)

/** Aggregate counts for the responder dashboard: pending (unattended) vs handled, split by kind. */
data class SosStats(
    val pendingSos: Int,
    val pendingSafe: Int,
    val handledSos: Int,
    val handledSafe: Int,
)
