package com.jyodroid.kunasismoayuda.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.NewSearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchReport
import com.jyodroid.kunasismoayuda.core.domain.model.SearchStatus
import com.jyodroid.kunasismoayuda.core.domain.model.SearchSubject
import com.jyodroid.kunasismoayuda.core.domain.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = true,
    val reports: List<SearchReport> = emptyList(),
    val error: Boolean = false,
    val subjectFilter: SearchSubject? = null, // null = all
    val statusFilter: SearchStatus? = null,   // null = all
)

data class SearchCreateState(
    val isSubmitting: Boolean = false,
    val error: Boolean = false,
)

/** Lost & Found / reunification board (pets + people). Reads + direct posting with an optional photo. */
class SearchViewModel(
    private val repository: SearchRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _createState = MutableStateFlow(SearchCreateState())
    val createState: StateFlow<SearchCreateState> = _createState.asStateFlow()

    var country: Country = Country.DEFAULT
        private set

    init {
        load()
    }

    fun setCountry(country: Country) {
        if (country == this.country) return
        this.country = country
        // Drop the previous country's reports so a failed reload doesn't show wrong-country data.
        _state.update { SearchUiState(isLoading = true, subjectFilter = it.subjectFilter, statusFilter = it.statusFilter) }
        load()
    }

    fun setSubject(subject: SearchSubject?) {
        _state.update { it.copy(subjectFilter = subject) }
        load()
    }

    fun setStatus(status: SearchStatus?) {
        _state.update { it.copy(statusFilter = status) }
        load()
    }

    fun load() {
        val subject = _state.value.subjectFilter
        val status = _state.value.statusFilter
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.list(subject, status, country.code) }
                .onSuccess { reports -> _state.update { it.copy(isLoading = false, reports = reports, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
        }
    }

    fun submit(report: NewSearchReport, photo: ByteArray?, mime: String?, onSuccess: () -> Unit) {
        _createState.update { it.copy(isSubmitting = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.create(report.copy(country = country.code), photo, mime) }
                .onSuccess {
                    _createState.update { it.copy(isSubmitting = false) }
                    load()
                    onSuccess()
                }
                .onFailure { _createState.update { it.copy(isSubmitting = false, error = true) } }
        }
    }

    /** Moderator-only: remove an abusive report (requires a logged-in admin token). */
    fun delete(id: Int) {
        viewModelScope.launch {
            runCatching { repository.delete(id) }
                .onSuccess { _state.update { s -> s.copy(reports = s.reports.filterNot { it.id == id }) } }
                .onFailure { _state.update { it.copy(error = true) } }
        }
    }

    fun resetCreateState() = _createState.update { SearchCreateState() }
}
