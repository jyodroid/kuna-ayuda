package com.jyodroid.kunasismoayuda.core.domain.model

/** The affected-place city list for a given [Country], used by the prioritization + map logic. */
object CountryRegions {
    fun of(country: Country): List<Region> = when (country) {
        Country.COLOMBIA -> ColombiaRegions.all
        Country.INDONESIA -> IndonesiaRegions.all
        Country.SPAIN -> SpainRegions.all
        Country.ITALY -> ItalyRegions.all
    }
}
