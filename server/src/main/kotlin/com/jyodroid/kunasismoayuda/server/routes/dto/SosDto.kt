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
    val createdAt: String,
    val handledAt: String? = null,
    val handledBy: String? = null,
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
)
