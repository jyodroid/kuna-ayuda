package com.jyodroid.kunasismoayuda.server.domain.models

import java.time.LocalDate

/** A moderated shelter / aid-collection point (acopio). Server-side domain model. */
data class Shelter(
    val id: Int,
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String,
    val hours: String?,
    val contactPhone: String?,
    val verified: Boolean,
    val lastVerified: LocalDate?,
    val active: Boolean,
    val country: String,
)

/** Fields an administrator supplies when creating a shelter. */
data class NewShelter(
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String,
    val hours: String?,
    val contactPhone: String?,
    val country: String = "CO",
)
