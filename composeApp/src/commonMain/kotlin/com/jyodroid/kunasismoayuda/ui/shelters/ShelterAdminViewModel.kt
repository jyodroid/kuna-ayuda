package com.jyodroid.kunasismoayuda.ui.shelters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.NewShelter
import com.jyodroid.kunasismoayuda.core.domain.repository.ShelterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShelterAdminState(
    val isSubmitting: Boolean = false,
    val error: Boolean = false,
)

/** Moderator-only: create a shelter / collection point via `POST /api/shelters` (bearer token). */
class ShelterAdminViewModel(
    private val repository: ShelterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ShelterAdminState())
    val state: StateFlow<ShelterAdminState> = _state.asStateFlow()

    /** Create a new point, or (when [editingId] is non-null) update the existing one. */
    fun submit(shelter: NewShelter, editingId: Int?, onSuccess: () -> Unit) {
        _state.update { it.copy(isSubmitting = true, error = false) }
        viewModelScope.launch {
            runCatching { if (editingId == null) repository.create(shelter) else repository.update(editingId, shelter) }
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                .onFailure { _state.update { it.copy(isSubmitting = false, error = true) } }
        }
    }

    fun delete(id: Int, onSuccess: () -> Unit) {
        _state.update { it.copy(isSubmitting = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                .onFailure { _state.update { it.copy(isSubmitting = false, error = true) } }
        }
    }

    fun resetState() = _state.update { ShelterAdminState() }
}
