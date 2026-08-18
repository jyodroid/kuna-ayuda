package com.jyodroid.kunasismoayuda.core.data.offline

import com.jyodroid.kunasismoayuda.core.data.remote.SosRequestDto
import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import kotlinx.serialization.Serializable

/**
 * A report persisted in the offline outbox. Mirrors [NewSos] with a stable [id] (so it can be
 * removed once delivered) and a [createdAtEpochMs] for age/ordering. Kept flat and `@Serializable`
 * so the whole outbox can be written to a single JSON file.
 */
@Serializable
data class QueuedSos(
    val id: String,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val region: String? = null,
    val message: String? = null,
    val contactPhone: String? = null,
    val createdAtEpochMs: Long,
    val attempts: Int = 0,
) {
    fun toRequestDto(): SosRequestDto = SosRequestDto(
        status = status,
        latitude = latitude,
        longitude = longitude,
        region = region,
        message = message,
        contactPhone = contactPhone,
    )

    companion object {
        fun from(id: String, createdAtEpochMs: Long, sos: NewSos): QueuedSos = QueuedSos(
            id = id,
            status = sos.status.name,
            latitude = sos.latitude,
            longitude = sos.longitude,
            region = sos.region,
            message = sos.message,
            contactPhone = sos.contactPhone,
            createdAtEpochMs = createdAtEpochMs,
        )
    }
}
