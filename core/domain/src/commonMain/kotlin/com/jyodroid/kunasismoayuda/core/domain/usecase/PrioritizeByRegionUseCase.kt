package com.jyodroid.kunasismoayuda.core.domain.usecase

import com.jyodroid.kunasismoayuda.core.domain.model.AffectedRegion
import com.jyodroid.kunasismoayuda.core.domain.model.ColombiaRegions
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.model.Region
import com.jyodroid.kunasismoayuda.core.domain.util.Geo
import kotlin.math.ln

/**
 * Ranks quakes by likely human impact: a larger magnitude closer to a populous region scores
 * higher. Recency is used as a tie-breaker. This surfaces "matters to people now" events first,
 * rather than a purely chronological list.
 *
 * The [regions] used are country-specific — callers pass the selected country's city list
 * (`CountryRegions.of(country)`) to [invoke]/[affectedRegions]. The constructor default keeps
 * existing single-country callers (and tests) working with Colombia.
 */
class PrioritizeByRegionUseCase(
    private val defaultRegions: List<Region> = ColombiaRegions.all,
) {
    operator fun invoke(
        quakes: List<Quake>,
        regions: List<Region> = defaultRegions,
    ): List<Quake> =
        quakes.sortedWith(
            compareByDescending<Quake> { impactScore(it, regions) }
                .thenByDescending { it.timeMillis },
        )

    /** Regions likely affected by a quake, nearest first (within [radiusKm]). */
    fun affectedRegions(
        quake: Quake,
        regions: List<Region> = defaultRegions,
        radiusKm: Double = 300.0,
    ): List<AffectedRegion> =
        regions.map { region ->
            val d = Geo.distanceKm(quake.latitude, quake.longitude, region.latitude, region.longitude)
            AffectedRegion(region = region, distanceKm = d, impactScore = regionImpact(quake, region, d))
        }.filter { it.distanceKm <= radiusKm }
            .sortedBy { it.distanceKm }

    private fun impactScore(quake: Quake, regions: List<Region>): Double =
        regions.maxOfOrNull { regionImpact(quake, it, Geo.distanceKm(quake.latitude, quake.longitude, it.latitude, it.longitude)) }
            ?: (quake.magnitude ?: 0.0)

    private fun regionImpact(quake: Quake, region: Region, distanceKm: Double): Double {
        val mag = quake.magnitude ?: 0.0
        // Attenuate magnitude with distance; weight lightly by population (log scale).
        val distanceFactor = 1.0 / (1.0 + distanceKm / 50.0)
        val populationFactor = 1.0 + ln((region.population.coerceAtLeast(1)).toDouble()) / 20.0
        return mag * distanceFactor * populationFactor
    }
}
