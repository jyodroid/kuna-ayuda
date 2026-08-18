package com.jyodroid.kunasismoayuda.core.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ShelterDto(
    val id: Int,
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String = "",
    val hours: String? = null,
    val contactPhone: String? = null,
    val verified: Boolean = true,
    val lastVerified: String? = null,
)

/** Body for `POST /api/shelters` (admin). Matches the server's `ShelterRequest`. */
@Serializable
data class NewShelterDto(
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
