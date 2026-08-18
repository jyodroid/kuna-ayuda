package com.jyodroid.kunasismoayuda.core.domain.model

enum class SosStatus { SOS, SAFE }

/** Whether a report was delivered to the server now, or stored offline to be retried later. */
enum class SosSendResult { SENT, QUEUED }

/** A geolocated SOS or an "I'm safe" check-in the user sends to responders. */
data class NewSos(
    val status: SosStatus,
    val latitude: Double?,
    val longitude: Double?,
    val region: String?,
    val message: String?,
    val contactPhone: String?,
)

/**
 * A submitted report as seen by a responder (moderator). Read-only; [createdAt] is the server's ISO
 * timestamp string. Coordinates are present for most SOS reports and absent for SAFE check-ins.
 */
data class SosReport(
    val id: Int,
    val status: SosStatus,
    val latitude: Double?,
    val longitude: Double?,
    val region: String?,
    val message: String?,
    val contactPhone: String?,
    val createdAt: String,
    // Responder lifecycle: null = pending/active; set = archived (attended for SOS, notified for SAFE).
    val handledAt: String? = null,
    val handledBy: String? = null,
) {
    val isHandled: Boolean get() = handledAt != null
}

/** Pending-vs-handled counts for the responder dashboard. */
data class SosStats(
    val pendingSos: Int,
    val pendingSafe: Int,
    val handledSos: Int,
    val handledSafe: Int,
) {
    val pendingTotal: Int get() = pendingSos + pendingSafe
    val handledTotal: Int get() = handledSos + handledSafe
}
