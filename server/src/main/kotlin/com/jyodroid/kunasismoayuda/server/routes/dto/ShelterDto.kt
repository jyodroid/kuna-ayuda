package com.jyodroid.kunasismoayuda.server.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShelterResponse(
    val id: Int,
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String,
    val hours: String? = null,
    val contactPhone: String? = null,
    val verified: Boolean,
    val lastVerified: String? = null, // ISO-8601 date
)

@Serializable
data class ShelterRequest(
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String = "",
    val hours: String? = null,
    val contactPhone: String? = null,
    val country: String = "CO",
)
