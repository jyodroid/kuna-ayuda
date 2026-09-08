package com.jyodroid.kunasismoayuda.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.NewSos
import com.jyodroid.kunasismoayuda.core.domain.model.SafeCheckIn
import com.jyodroid.kunasismoayuda.core.domain.model.SosSendResult
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lifecycle of publishing an "I'm safe" check-in (offline-aware). */
enum class SafeSendPhase { IDLE, SENDING, SENT, QUEUED, ERROR }

data class SafeUiState(
    val isLoading: Boolean = true,
    val checkIns: List<SafeCheckIn> = emptyList(),
    val error: Boolean = false,
    val sendPhase: SafeSendPhase = SafeSendPhase.IDLE,
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

    /**
     * Publish a public "I'm safe" check-in. Offline-first: `repository.send` persists to the outbox and
     * retries, so a check-in made with no signal is delivered once the connection returns (QUEUED). On
     * SENT we reload the list so it appears. [name] is required (the poster confirmed it goes public).
     */
    fun sendSafe(name: String, region: String) {
        _state.update { it.copy(sendPhase = SafeSendPhase.SENDING) }
        viewModelScope.launch {
            runCatching {
                repository.send(
                    NewSos(
                        status = SosStatus.SAFE,
                        latitude = null,
                        longitude = null,
                        region = region.trim().ifBlank { null },
                        message = null,
                        contactPhone = null,
                        displayName = name.trim().ifBlank { null },
                        country = country.code,
                    ),
                )
            }
                .onSuccess { result ->
                    val phase = if (result == SosSendResult.SENT) SafeSendPhase.SENT else SafeSendPhase.QUEUED
                    _state.update { it.copy(sendPhase = phase) }
                    if (result == SosSendResult.SENT) load()
                }
                .onFailure { _state.update { it.copy(sendPhase = SafeSendPhase.ERROR) } }
        }
    }

    /** Reset the send status (e.g. after the confirmation is shown/dismissed). */
    fun clearSendPhase() = _state.update { it.copy(sendPhase = SafeSendPhase.IDLE) }

    /** Moderator-only: remove a fake/abusive "I'm safe" post (requires a logged-in admin token). */
    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess { _state.update { s -> s.copy(checkIns = s.checkIns.filterNot { it.id == id }) } }
                .onFailure { _state.update { it.copy(error = true) } }
        }
    }
}
