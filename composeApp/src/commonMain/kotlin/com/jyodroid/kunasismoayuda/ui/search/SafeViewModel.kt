package com.jyodroid.kunasismoayuda.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.SafeCheckIn
import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SafeUiState(
    val isLoading: Boolean = true,
    val checkIns: List<SafeCheckIn> = emptyList(),
    val error: Boolean = false,
)

/** Public "I'm safe" reassurance list (Búsqueda y reencuentro → A salvo). Read-only, per country. */
class SafeViewModel(
    private val repository: SosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SafeUiState())
    val state: StateFlow<SafeUiState> = _state.asStateFlow()

    var country: Country = Country.DEFAULT
        private set

    init {
        load()
    }

    fun setCountry(country: Country) {
        if (country == this.country) return
        this.country = country
        _state.value = SafeUiState(isLoading = true)
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.listPublicSafe(country.code) }
                .onSuccess { list -> _state.update { it.copy(isLoading = false, checkIns = list, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
    }

    /** Moderator-only: remove a fake/abusive "I'm safe" post (requires a logged-in admin token). */
    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess { _state.update { s -> s.copy(checkIns = s.checkIns.filterNot { it.id == id }) } }
                .onFailure { _state.update { it.copy(error = true) } }
        }
    }
}
