package com.jyodroid.kunasismoayuda.core.domain.usecase

import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryRegions
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.repository.QuakeRepository

/**
 * Fetches recent quakes for a [Country] and returns them prioritized by likely impact on that
 * country's populated regions.
 */
class GetQuakesUseCase(
    private val repository: QuakeRepository,
    private val prioritize: PrioritizeByRegionUseCase = PrioritizeByRegionUseCase(),
) {
    suspend operator fun invoke(
        minMagnitude: Double = 2.5,
        country: Country = Country.DEFAULT,
    ): List<Quake> {
        val quakes = repository.getQuakes(minMagnitude, country.code)
        return prioritize(quakes, CountryRegions.of(country))
    }
}
