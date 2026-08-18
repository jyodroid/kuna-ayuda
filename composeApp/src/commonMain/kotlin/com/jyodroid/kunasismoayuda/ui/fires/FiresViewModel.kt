package com.jyodroid.kunasismoayuda.ui.fires

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.AffectedRegion
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryRegions
import com.jyodroid.kunasismoayuda.core.domain.model.Fire
import com.jyodroid.kunasismoayuda.core.domain.model.FireIntensity
import com.jyodroid.kunasismoayuda.core.domain.model.Region
import com.jyodroid.kunasismoayuda.core.domain.repository.FireRepository
import com.jyodroid.kunasismoayuda.core.domain.util.Geo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FiresUiState(
    val isLoading: Boolean = true,
    val fires: List<Fire> = emptyList(),
    val error: Boolean = false,
)

/** Nearest known city to a fire + how far it is (km) — used to label FIRMS hotspots that carry no place. */
data class FirePlace(val name: String, val distanceKm: Double)

/**
 * Second-hazard vertical, mirroring [com.jyodroid.kunasismoayuda.ui.quakes.QuakesViewModel]: loads the
 * per-country active-wildfire feed (`GET /api/fires`) and derives the featured fire + affected places
 * the same way quakes do, reusing [CountryRegions] and [Geo].
 */
class FiresViewModel(
    private val repository: FireRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FiresUiState())
    val state: StateFlow<FiresUiState> = _state.asStateFlow()

    var country: Country = Country.DEFAULT
        private set

    init {
        load()
    }

    fun setCountry(country: Country) {
        if (country == this.country) return
        this.country = country
        _state.value = FiresUiState(isLoading = true)
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.getFires(country.code) }
                .onSuccess { fires -> _state.update { it.copy(isLoading = false, fires = fires, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
    }

    /**
     * The fire to headline, **ranked toward populated areas**: a fire's relevance is its intensity
     * discounted by distance to the nearest known city — a big fire in the uninhabited wilderness
     * (e.g. the Amazon) matters less to users than a smaller one near people. Recency breaks ties.
     * Falls back to intensity+recency if the country has no region list.
     */
    val featuredFire: Fire?
        get() {
            val fires = _state.value.fires
            if (fires.isEmpty()) return null
            val regions = CountryRegions.of(country)
            return fires.maxWithOrNull(
                compareBy<Fire> { impactScore(it, regions) }.thenBy { it.timeMillis },
            )
        }

    /** All active fires, **most relevant first** (proximity-weighted intensity, recency breaks ties). */
    fun rankedFires(): List<Fire> {
        val regions = CountryRegions.of(country)
        return _state.value.fires.sortedWith(
            compareByDescending<Fire> { impactScore(it, regions) }.thenByDescending { it.timeMillis },
        )
    }

    /** Populated places within [radiusKm] of a **specific** fire (for that fire's detail screen). */
    fun affectedRegionsFor(fire: Fire, radiusKm: Double = 150.0): List<AffectedRegion> =
        CountryRegions.of(country).mapNotNull { region ->
            val km = Geo.distanceKm(fire.latitude, fire.longitude, region.latitude, region.longitude)
            if (km <= radiusKm) AffectedRegion(region = region, distanceKm = km, impactScore = 1.0 / (1.0 + km)) else null
        }.sortedBy { it.distanceKm }

    /** Intensity (FRP or a per-bucket proxy) discounted by distance to the nearest populated place. */
    private fun impactScore(fire: Fire, regions: List<Region>): Double {
        val nearestKm = regions.minOfOrNull {
            Geo.distanceKm(fire.latitude, fire.longitude, it.latitude, it.longitude)
        } ?: return fire.intensity.ordinal.toDouble()
        val intensity = fire.frpMw ?: when (fire.intensity) {
            FireIntensity.HIGH -> 100.0
            FireIntensity.MODERATE -> 25.0
            FireIntensity.LOW -> 5.0
        }
        return intensity / (1.0 + nearestKm / PROXIMITY_HALF_LIFE_KM)
    }

    /**
     * The nearest known city to [fire] plus its distance. FIRMS points (our primary source) are raw
     * satellite hotspots with no place name; instead of "unknown location" the UI names the nearest
     * city, honestly labelled by distance ("Cerca de X" when close, "A N km de X" when far). Returns
     * null only if the country has no region list. GDACS events already carry a place, so this only
     * kicks in for FIRMS.
     */
    fun nearestPlace(fire: Fire): FirePlace? =
        CountryRegions.of(country)
            .map { region -> region to Geo.distanceKm(fire.latitude, fire.longitude, region.latitude, region.longitude) }
            .minByOrNull { it.second }
            ?.let { (region, km) -> FirePlace(region.name, km) }

    /** Populated places within [radiusKm] of any active fire, nearest first (deduped by region). */
    fun affectedRegions(radiusKm: Double = 150.0): List<AffectedRegion> {
        val fires = _state.value.fires
        if (fires.isEmpty()) return emptyList()
        return CountryRegions.of(country).mapNotNull { region ->
            val nearest = fires.minOf { f ->
                Geo.distanceKm(f.latitude, f.longitude, region.latitude, region.longitude)
            }
            if (nearest <= radiusKm) {
                AffectedRegion(region = region, distanceKm = nearest, impactScore = 1.0 / (1.0 + nearest))
            } else {
                null
            }
        }.sortedBy { it.distanceKm }
    }

    private companion object {
        // Distance at which a fire's intensity is halved for headline ranking (proximity weighting).
        const val PROXIMITY_HALF_LIFE_KM = 50.0
    }
}
