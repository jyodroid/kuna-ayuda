package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A moderated shelter / aid-collection point (acopio). Only administrators publish these
 * server-side, so entries the app shows are official (anti-fraud).
 */
data class Shelter(
    val id: Int,
    val name: String,
    val type: ShelterType,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val accepts: String,
    val hours: String?,
    val contactPhone: String?,
    val verified: Boolean,
    val lastVerified: String?, // ISO-8601 date, as provided by the backend
)

enum class ShelterType {
    ACOPIO,     // collection point
    ALBERGUE,   // shelter
    SALUD,      // medical
    AGUA,       // water
    OTRO;       // other / unknown

    companion object {
        fun fromRaw(raw: String): ShelterType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OTRO
    }
}
