package com.jyodroid.kunasismoayuda.ui.sos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.SosReport
import com.jyodroid.kunasismoayuda.core.domain.model.SosStats
import com.jyodroid.kunasismoayuda.core.domain.model.SosStatus
import com.jyodroid.kunasismoayuda.core.domain.repository.SosRepository
import com.jyodroid.kunasismoayuda.core.location.LocationProvider
import com.jyodroid.kunasismoayuda.core.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SosResponderUiState(
    val isLoading: Boolean = true,
    val reports: List<SosReport> = emptyList(),
    val error: Boolean = false,
    val filter: SosStatus? = SosStatus.SOS, // default to the people who need help; null = all
    val showArchived: Boolean = false,      // false = active/pending list; true = archived list
    val stats: SosStats? = null,
    val actioningId: Int? = null,           // a report whose archive/reopen/delete is in flight
    // Moderator location, for grouping reports by proximity (Cerca / Misma zona / Lejos / Sin ubicación).
    val moderatorLat: Double? = null,
    val moderatorLon: Double? = null,
    val locationDenied: Boolean = false,    // set if the moderator denied/blocked location
)

/**
 * Responder view over the moderator-only `/api/sos`. Lists submitted SOS alerts and "I'm safe"
 * check-ins, tracks a pending-vs-handled dashboard, and drives the attended/notified lifecycle:
 * archive (handle), reopen, and permanent delete.
 */
class SosResponderViewModel(
    private val repository: SosRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(SosResponderUiState())
    val state: StateFlow<SosResponderUiState> = _state.asStateFlow()

    /** Ask for the moderator's location so reports can be grouped by distance (on-demand permission). */
    fun requestLocation() {
        viewModelScope.launch {
            when (val r = locationProvider.current()) {
                is LocationResult.Granted ->
                    _state.update { it.copy(moderatorLat = r.coordinates.latitude, moderatorLon = r.coordinates.longitude, locationDenied = false) }
                else -> _state.update { it.copy(locationDenied = true) }
            }
        }
    }

    fun setFilter(filter: SosStatus?) {
        if (filter == _state.value.filter) return
        _state.update { it.copy(filter = filter) }
        load()
    }

    fun setShowArchived(showArchived: Boolean) {
        if (showArchived == _state.value.showArchived) return
        _state.update { it.copy(showArchived = showArchived) }
        load()
    }

    fun load() {
        val filter = _state.value.filter
        val archived = _state.value.showArchived
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.listActive(filter, archived) }
                .onSuccess { reports -> _state.update { it.copy(isLoading = false, reports = reports, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            runCatching { repository.stats() }.onSuccess { s -> _state.update { it.copy(stats = s) } }
        }
    }

    /** Archive as attended (SOS) / notified (SAFE). */
    fun archive(id: Int) = act(id) { repository.markHandled(id) }

    /** Restore an archived report to the active list. */
    fun reopen(id: Int) = act(id) { repository.reopen(id) }

    /** Permanently delete a report. */
    fun delete(id: Int) = act(id) { repository.delete(id) }

    private fun act(id: Int, block: suspend () -> Unit) {
        if (_state.value.actioningId != null) return
        _state.update { it.copy(actioningId = id) }
        viewModelScope.launch {
            val result = runCatching { block() }
            _state.update { it.copy(actioningId = null) }
            result.onSuccess { load() }
                .onFailure { _state.update { it.copy(error = true) } }
        }
    }
}
