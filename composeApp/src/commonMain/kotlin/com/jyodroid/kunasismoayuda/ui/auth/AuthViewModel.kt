package com.jyodroid.kunasismoayuda.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.Session
import com.jyodroid.kunasismoayuda.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginFormState(
    val isSubmitting: Boolean = false,
    val error: Boolean = false,
)

/**
 * Moderator sign-in. The session it exposes is what gates the moderation queue: null means nobody
 * is logged in (the default for every regular user). Only this flow ever obtains a token.
 */
class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {

    val session: StateFlow<Session?> = repository.session

    /** True when the moderator was auto-logged-out by an expired token; drives the login-screen hint. */
    val sessionExpired: StateFlow<Boolean> = repository.sessionExpired

    private val _form = MutableStateFlow(LoginFormState())
    val form: StateFlow<LoginFormState> = _form.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _form.update { it.copy(error = true) }
            return
        }
        _form.update { it.copy(isSubmitting = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.login(email, password) }
                .onSuccess { _form.update { LoginFormState() } }
                .onFailure { _form.update { it.copy(isSubmitting = false, error = true) } }
        }
    }

    fun logout() = repository.logout()

    fun resetForm() = _form.update { LoginFormState() }
}
