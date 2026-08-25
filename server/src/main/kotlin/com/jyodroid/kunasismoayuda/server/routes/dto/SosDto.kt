package com.jyodroid.kunasismoayuda.server.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class SosResponse(
    val id: Int,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val region: String? = null,
    val message: String? = null,
    val contactPhone: String? = null,
    val displayName: String? = null,
    val createdAt: String,
    val handledAt: String? = null,
    val handledBy: String? = null,
)

/** A public "I'm safe" check-in for the community reassurance list. Name + region + time only. */
@Serializable
data class SafeCheckInResponse(
    val id: Int,
    val name: String,
    val region: String? = null,
    val createdAtEpochMs: Long,
)

@Serializable
data class SosStatsResponse(
    val pendingSos: Int,
    val pendingSafe: Int,
    val handledSos: Int,
    val handledSafe: Int,
)

@Serializable
data class SosRequest(
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val region: String? = null,
    val message: String? = null,
    val contactPhone: String? = null,
    // Public name for a SAFE check-in (required to appear on the public list). Ignored for SOS coords.
    val displayName: String? = null,
    // ISO country code so the check-in shows on the right country's public safe list.
    val country: String? = null,
)
