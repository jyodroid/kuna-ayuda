package com.jyodroid.kunasismoayuda.core.domain.util

import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryRegions
import com.jyodroid.kunasismoayuda.core.domain.model.Region

/**
 * Best-effort mapping of a free-text region/city string (as typed on an aid-board post) to a known
 * [Region] in the country's city list, so posts can be sorted by rough distance to the user.
 *
 * This is intentionally fuzzy — posts carry only a region *name*, not coordinates. Matching is:
 * case/space-insensitive exact match first, then a substring match either direction (longest known
 * region name wins, so "Cali, Valle" resolves to "Cali"). Returns null when nothing matches, so the
 * caller can sort unmatched posts last rather than guessing.
 */
object RegionLocate {
    fun match(regionText: String?, country: Country): Region? {
        val q = regionText?.trim()?.lowercase()?.takeIf { it.isNotEmpty() } ?: return null
        val regions = CountryRegions.of(country)
        regions.firstOrNull { it.name.trim().lowercase() == q }?.let { return it }
        return regions
            .filter { r ->
                val name = r.name.trim().lowercase()
                name.isNotEmpty() && (q.contains(name) || name.contains(q))
            }
            .maxByOrNull { it.name.length }
    }
}
