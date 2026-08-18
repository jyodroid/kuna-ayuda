package com.jyodroid.kunasismoayuda.core.domain.model

/** Fields an admin submits to create a shelter/collection point (`POST /api/shelters`). */
data class NewShelter(
    val name: String,
    val type: ShelterType,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String = "",
    val hours: String? = null,
    val contactPhone: String? = null,
    val country: String = "CO",
)
