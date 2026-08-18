package com.jyodroid.kunasismoayuda.ui.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.ResourcePost
import com.jyodroid.kunasismoayuda.core.domain.repository.ResourceBoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModerationState(
    val isLoading: Boolean = false,
    val pending: List<ResourcePost> = emptyList(),
    val error: Boolean = false,
    val actioningId: Int? = null, // the post currently being approved/rejected
)

/** The admin moderation queue: list PENDING posts, approve (publish) or reject (remove) each. */
class ModerationViewModel(
    private val repository: ResourceBoardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ModerationState())
    val state: StateFlow<ModerationState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.listPending() }
                .onSuccess { posts -> _state.update { it.copy(isLoading = false, pending = posts, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
    }

    fun approve(id: Int) = act(id) { repository.approve(id) }

    fun reject(id: Int) = act(id) { repository.reject(id) }

    private fun act(id: Int, block: suspend () -> Unit) {
        _state.update { it.copy(actioningId = id, error = false) }
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess {
                    // Drop the handled post from the local queue without a full reload.
                    _state.update { s -> s.copy(actioningId = null, pending = s.pending.filterNot { it.id == id }) }
                }
                .onFailure { _state.update { it.copy(actioningId = null, error = true) } }
        }
    }
}
