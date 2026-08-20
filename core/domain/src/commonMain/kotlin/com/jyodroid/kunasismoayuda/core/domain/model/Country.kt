package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A supported country. The client uses [code] to ask the server for that country's quake feed
 * (`GET /api/quakes?country=CO`), shelters, and aid board, and uses [centerLat]/[centerLon]/
 * [defaultZoom] to frame the map when there are no affected help points to center on.
 *
 * The earthquake bounding box lives on the server (`CountryBBoxes`); the per-country city lists used
 * for affected-place logic live in [CountryRegions]. Keep the two in sync when adding a country.
 */
enum class Country(
    val code: String,
    val displayNameEs: String,
    val displayNameEn: String,
    val flag: String,
    val centerLat: Double,
    val centerLon: Double,
    val defaultZoom: Double,
) {
    COLOMBIA("CO", "Colombia", "Colombia", "🇨🇴", 4.6, -74.1, 5.0),
    INDONESIA("ID", "Indonesia", "Indonesia", "🇮🇩", -2.5, 118.0, 4.0),
    SPAIN("ES", "España", "Spain", "🇪🇸", 40.0, -3.7, 5.0),
    ITALY("IT", "Italia", "Italy", "🇮🇹", 42.5, 12.5, 5.0),
    PERU("PE", "Perú", "Peru", "🇵🇪", -9.2, -75.0, 5.0);

    companion object {
        val DEFAULT: Country = COLOMBIA

        /** Resolve a stored/serialized code back to a [Country], defaulting when unknown/null. */
        fun fromCode(code: String?): Country =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
    }
}
