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

/** Which moderation list is shown: posts awaiting review, or already-published posts. */
enum class ModerationTab { PENDING, PUBLISHED }

data class ModerationState(
    val isLoading: Boolean = false,
    val tab: ModerationTab = ModerationTab.PENDING,
    val pending: List<ResourcePost> = emptyList(),
    val active: List<ResourcePost> = emptyList(),
    val error: Boolean = false,
    val actioningId: Int? = null, // the post currently being approved/rejected/deleted
)

/**
 * Admin moderation: the PENDING queue (approve/reject) and the PUBLISHED list (delete a live post
 * immediately). Reject and delete both go through [ResourceBoardRepository.reject] → DELETE.
 */
class ModerationViewModel(
    private val repository: ResourceBoardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ModerationState())
    val state: StateFlow<ModerationState> = _state.asStateFlow()

    /** Initial load — the pending queue (the default tab). */
    fun load() = loadPending()

    fun selectTab(tab: ModerationTab) {
        _state.update { it.copy(tab = tab) }
        when (tab) {
            ModerationTab.PENDING -> loadPending()
            ModerationTab.PUBLISHED -> loadActive()
        }
    }

    fun loadPending() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.listPending() }
                .onSuccess { posts -> _state.update { it.copy(isLoading = false, pending = posts, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
    }

    fun loadActive() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.listActive() }
                .onSuccess { posts -> _state.update { it.copy(isLoading = false, active = posts, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
    }

    fun approve(id: Int) = act(id) { repository.approve(id) }

    /** Reject a pending post, or delete a published one (same DELETE either way). */
    fun reject(id: Int) = act(id) { repository.reject(id) }

    private fun act(id: Int, block: suspend () -> Unit) {
        _state.update { it.copy(actioningId = id, error = false) }
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess {
                    // Drop the handled post from whichever list held it, without a full reload.
                    _state.update { s ->
                        s.copy(
                            actioningId = null,
                            pending = s.pending.filterNot { it.id == id },
                            active = s.active.filterNot { it.id == id },
                        )
                    }
                }
                .onFailure { _state.update { it.copy(actioningId = null, error = true) } }
        }
    }
}
