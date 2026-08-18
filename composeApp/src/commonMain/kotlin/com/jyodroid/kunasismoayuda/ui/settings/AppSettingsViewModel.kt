package com.jyodroid.kunasismoayuda.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.data.settings.CountryStore
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * App-wide country selection. Distinguishes "still reading from disk" ([CountryState.Loading]) from
 * "never chosen" ([CountryState.NeedsSelection]) so a returning user doesn't flash the picker.
 */
sealed interface CountryState {
    data object Loading : CountryState
    data object NeedsSelection : CountryState
    data class Selected(val country: Country) : CountryState
}

class AppSettingsViewModel(
    private val store: CountryStore,
) : ViewModel() {

    private val _state = MutableStateFlow<CountryState>(CountryState.Loading)
    val state: StateFlow<CountryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = store.get()
            _state.value = if (stored != null) CountryState.Selected(stored) else CountryState.NeedsSelection
        }
    }

    /** Persist and apply a country (from the first-run picker or the Overview switcher). */
    fun select(country: Country) {
        _state.value = CountryState.Selected(country)
        viewModelScope.launch { store.set(country) }
    }
}
