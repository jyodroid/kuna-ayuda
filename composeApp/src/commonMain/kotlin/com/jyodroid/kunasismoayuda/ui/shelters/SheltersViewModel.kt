package com.jyodroid.kunasismoayuda.ui.shelters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.Shelter
import com.jyodroid.kunasismoayuda.core.domain.repository.ShelterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SheltersUiState(
    val isLoading: Boolean = true,
    val shelters: List<Shelter> = emptyList(),
    val error: Boolean = false,
)

class SheltersViewModel(
    private val repository: ShelterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SheltersUiState())
    val state: StateFlow<SheltersUiState> = _state.asStateFlow()

    var country: Country = Country.DEFAULT
        private set

    init {
        load()
    }

    fun setCountry(country: Country) {
        if (country == this.country) return
        this.country = country
        // Drop the previous country's data immediately, so if the new load fails we show empty/error
        // rather than the wrong country's shelters.
        _state.value = SheltersUiState(isLoading = true)
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.getShelters(country.code) }
                .onSuccess { shelters ->
                    _state.update { it.copy(isLoading = false, shelters = shelters, error = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = true) }
                }
        }
    }
}
