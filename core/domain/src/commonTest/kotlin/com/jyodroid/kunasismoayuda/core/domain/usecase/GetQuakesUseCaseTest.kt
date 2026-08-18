package com.jyodroid.kunasismoayuda.core.domain.usecase

import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.repository.QuakeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetQuakesUseCaseTest {

    private class FakeQuakeRepository(private val quakes: List<Quake>) : QuakeRepository {
        var lastMinMagnitude: Double? = null
        var lastCountry: String? = null
        override suspend fun getQuakes(minMagnitude: Double, country: String): List<Quake> {
            lastMinMagnitude = minMagnitude
            lastCountry = country
            return quakes
        }
    }

    private fun quake(id: String, mag: Double, lat: Double, lon: Double) = Quake(
        id = id, timeMillis = 1, magnitude = mag, depthKm = 10.0,
        latitude = lat, longitude = lon, place = id, source = "TEST",
    )

    @Test
    fun passes_the_country_code_and_min_magnitude_to_the_repository() = runTest {
        val repo = FakeQuakeRepository(emptyList())
        GetQuakesUseCase(repo)(minMagnitude = 4.0, country = Country.SPAIN)
        assertEquals(4.0, repo.lastMinMagnitude)
        assertEquals(Country.SPAIN.code, repo.lastCountry)
    }

    @Test
    fun returns_quakes_prioritized_by_impact_on_the_country() = runTest {
        // A remote Pacific quake (bigger) and one near Bogotá (smaller). For Colombia, the near-city
        // one must rank first — i.e. the use case delegates to the prioritizer, not raw repo order.
        val remote = quake("remote", mag = 6.0, lat = 2.0, lon = -120.0)
        val nearCity = quake("near-city", mag = 5.0, lat = 4.7, lon = -74.1)
        val useCase = GetQuakesUseCase(FakeQuakeRepository(listOf(remote, nearCity)))

        val result = useCase(country = Country.COLOMBIA)

        assertEquals("near-city", result.first().id)
    }
}
