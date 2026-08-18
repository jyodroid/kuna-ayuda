package com.jyodroid.kunasismoayuda.ui.quakes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.AffectedRegion
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.CountryRegions
import com.jyodroid.kunasismoayuda.core.domain.model.Quake
import com.jyodroid.kunasismoayuda.core.domain.usecase.GetQuakesUseCase
import com.jyodroid.kunasismoayuda.core.domain.usecase.IdentifyAftershocksUseCase
import com.jyodroid.kunasismoayuda.core.domain.usecase.PrioritizeByRegionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuakesUiState(
    val isLoading: Boolean = true,
    val quakes: List<Quake> = emptyList(),
    val error: Boolean = false,
)

class QuakesViewModel(
    private val getQuakes: GetQuakesUseCase,
    private val prioritize: PrioritizeByRegionUseCase,
    private val identifyAftershocks: IdentifyAftershocksUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(QuakesUiState())
    val state: StateFlow<QuakesUiState> = _state.asStateFlow()

    /** The country whose feed is currently shown. Driven by the app-wide country selection. */
    var country: Country = Country.DEFAULT
        private set

    init {
        load()
    }

    /** Switch the country and reload the feed (no-op if unchanged). */
    fun setCountry(country: Country) {
        if (country == this.country) return
        this.country = country
        // Drop the previous country's feed immediately so a failed reload can't show wrong-country quakes.
        _state.value = QuakesUiState(isLoading = true)
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { getQuakes(country = country) }
                .onSuccess { quakes ->
                    _state.update { it.copy(isLoading = false, quakes = quakes, error = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = true) }
                }
        }
    }

    fun quakeById(id: String): Quake? = _state.value.quakes.firstOrNull { it.id == id }

    fun affectedRegions(quake: Quake): List<AffectedRegion> =
        prioritize.affectedRegions(quake, CountryRegions.of(country))

    /** Réplicas / nearby events for [quake], drawn from the current feed. */
    fun aftershocks(quake: Quake): List<Quake> = identifyAftershocks(quake, _state.value.quakes)
}
