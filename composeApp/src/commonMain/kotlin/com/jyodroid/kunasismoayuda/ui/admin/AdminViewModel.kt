package com.jyodroid.kunasismoayuda.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.AdminAccount
import com.jyodroid.kunasismoayuda.core.domain.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What went wrong creating an admin, so the UI can show a localized message. */
enum class CreateError { INVALID_EMAIL, SHORT_PASSWORD, DUPLICATE, GENERIC }

data class AdminState(
    val isLoading: Boolean = false,
    val admins: List<AdminAccount> = emptyList(),
    val error: Boolean = false,
    val isCreating: Boolean = false,
    val createError: CreateError? = null,
    val deletingId: Int? = null, // the account currently being deleted
)

/**
 * Super-admin console over `/api/admins`. Lists moderator accounts, creates new ones (always plain
 * ADMIN — the server never mints another superadmin via the API), and deletes them. The screen is
 * only reachable for a SUPERADMIN session; the server enforces the same via requireSuperAdmin().
 */
class AdminViewModel(
    private val repository: AdminRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.list() }
                .onSuccess { admins -> _state.update { it.copy(isLoading = false, admins = admins, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
    }

    /** Client-side mirror of the server's validation, for instant feedback; the server stays authoritative. */
    fun create(email: String, password: String) {
        val normalized = email.trim().lowercase()
        when {
            !normalized.contains("@") -> {
                _state.update { it.copy(createError = CreateError.INVALID_EMAIL) }
                return
            }
            password.length < MIN_PASSWORD -> {
                _state.update { it.copy(createError = CreateError.SHORT_PASSWORD) }
                return
            }
            _state.value.admins.any { it.email.equals(normalized, ignoreCase = true) } -> {
                _state.update { it.copy(createError = CreateError.DUPLICATE) }
                return
            }
        }
        _state.update { it.copy(isCreating = true, createError = null) }
        viewModelScope.launch {
            runCatching { repository.create(normalized, password) }
                .onSuccess { created ->
                    _state.update { s -> s.copy(isCreating = false, admins = s.admins + created) }
                }
                .onFailure {
                    _state.update { it.copy(isCreating = false, createError = CreateError.GENERIC) }
                }
        }
    }

    fun delete(id: Int) {
        _state.update { it.copy(deletingId = id, error = false) }
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess {
                    _state.update { s -> s.copy(deletingId = null, admins = s.admins.filterNot { it.id == id }) }
                }
                .onFailure { _state.update { it.copy(deletingId = null, error = true) } }
        }
    }

    /** Clears a create error once the user starts editing again. */
    fun clearCreateError() = _state.update { it.copy(createError = null) }

    private companion object {
        const val MIN_PASSWORD = 8
    }
}
