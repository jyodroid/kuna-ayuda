package com.jyodroid.kunasismoayuda.ui.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyodroid.kunasismoayuda.core.domain.model.ClassifiedPreview
import com.jyodroid.kunasismoayuda.core.domain.model.Country
import com.jyodroid.kunasismoayuda.core.domain.model.NewResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.PostKind
import com.jyodroid.kunasismoayuda.core.domain.model.ResourcePost
import com.jyodroid.kunasismoayuda.core.domain.model.ResourceType
import com.jyodroid.kunasismoayuda.core.data.remote.UnreadablePasteException
import com.jyodroid.kunasismoayuda.core.domain.repository.ResourceBoardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BoardUiState(
    val isLoading: Boolean = true,
    val posts: List<ResourcePost> = emptyList(),
    val error: Boolean = false,
    val kindFilter: PostKind = PostKind.REQUEST,
    val typeFilter: ResourceType? = null, // null = all resource types
    val ownedIds: Set<Int> = emptySet(),  // posts this device created — the ones it can resolve (#4)
    val resolvingId: Int? = null,         // a post whose resolve is in flight
)

data class CreateState(
    val isSubmitting: Boolean = false,
    val error: Boolean = false,
)

data class ClassifyState(
    val kind: PostKind = PostKind.REQUEST, // poster's choice, sent so the AI needn't guess REQUEST/OFFER
    val isSubmitting: Boolean = false,   // preview (analyze) call in flight
    val isConfirming: Boolean = false,   // confirm (send to moderator) call in flight
    val error: Boolean = false,          // generic failure
    val unreadable: Boolean = false,     // nothing to classify (a link/screenshot, or empty AI result) → 422
    val preview: ClassifiedPreview? = null, // AI result awaiting the poster's confirmation
    val pendingText: String? = null,     // original text kept so we can confirm without re-typing (text path)
    val cacheRef: String? = null,        // handle to confirm an IMAGE classify without re-uploading
    val sent: Boolean = false,           // confirmed → queued for a moderator
)

/** Aggregate counts of the whole (unfiltered) board, for the Overview summary. */
data class BoardSummary(
    val offers: Int = 0,
    val requests: Int = 0,
)

class BoardViewModel(
    private val repository: ResourceBoardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BoardUiState())
    val state: StateFlow<BoardUiState> = _state.asStateFlow()

    private val _createState = MutableStateFlow(CreateState())
    val createState: StateFlow<CreateState> = _createState.asStateFlow()

    private val _classifyState = MutableStateFlow(ClassifyState())
    val classifyState: StateFlow<ClassifyState> = _classifyState.asStateFlow()

    private val _summary = MutableStateFlow(BoardSummary())
    val summary: StateFlow<BoardSummary> = _summary.asStateFlow()

    /** The country whose board is currently shown. Driven by the app-wide country selection. */
    var country: Country = Country.DEFAULT
        private set

    init {
        load()
        loadSummary()
    }

    fun setCountry(country: Country) {
        if (country == this.country) return
        this.country = country
        // Drop the previous country's posts/summary so a failed reload doesn't show wrong-country data.
        _state.update { BoardUiState(isLoading = true, kindFilter = it.kindFilter, typeFilter = it.typeFilter) }
        _summary.value = BoardSummary()
        load()
        loadSummary()
    }

    /** Counts across the whole board (both kinds), independent of the active filter. */
    fun loadSummary() {
        viewModelScope.launch {
            runCatching { repository.list(null, null, null, country.code) }
                .onSuccess { all ->
                    _summary.value = BoardSummary(
                        offers = all.count { it.kind == PostKind.OFFER },
                        requests = all.count { it.kind == PostKind.REQUEST },
                    )
                }
        }
    }

    fun setKind(kind: PostKind) {
        _state.update { it.copy(kindFilter = kind) }
        load()
    }

    /** Filter by resource type; null clears it (all types). Kept in the VM so it survives tab switches. */
    fun setType(type: ResourceType?) {
        _state.update { it.copy(typeFilter = type) }
        load()
    }

    fun load() {
        val kind = _state.value.kindFilter
        val type = _state.value.typeFilter
        _state.update { it.copy(isLoading = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.list(kind, null, type, country.code) }
                .onSuccess { posts -> _state.update { it.copy(isLoading = false, posts = posts, error = false) } }
                .onFailure { _state.update { it.copy(isLoading = false, error = true) } }
            // Refresh which posts this device owns (drives the "resolve" affordance).
            runCatching { repository.ownedPostIds() }
                .onSuccess { ids -> _state.update { it.copy(ownedIds = ids) } }
        }
    }

    /** Resolve (close) a post this device created. No-op if we don't own it. */
    fun resolve(id: Int) {
        if (_state.value.resolvingId != null) return
        _state.update { it.copy(resolvingId = id) }
        viewModelScope.launch {
            runCatching { repository.resolve(id) }
            _state.update { it.copy(resolvingId = null) }
            load()
            loadSummary()
        }
    }

    fun submit(post: NewResourcePost, onSuccess: () -> Unit) {
        _createState.update { it.copy(isSubmitting = true, error = false) }
        viewModelScope.launch {
            runCatching { repository.create(post.copy(country = country.code)) }
                .onSuccess {
                    _createState.update { it.copy(isSubmitting = false) }
                    _state.update { it.copy(kindFilter = post.kind) }
                    load()
                    loadSummary()
                    onSuccess()
                }
                .onFailure { _createState.update { it.copy(isSubmitting = false, error = true) } }
        }
    }

    fun resetCreateState() {
        _createState.update { CreateState() }
    }

    /**
     * Step 1 — analyze: classify the pasted text and show the poster a PREVIEW to review. Nothing is
     * queued yet; the poster confirms via [confirmClassified].
     */
    /** Poster's REQUEST/OFFER choice (input step), sent with classify so the AI doesn't have to guess. */
    fun setClassifyKind(kind: PostKind) {
        _classifyState.update { it.copy(kind = kind) }
    }

    fun classify(text: String) {
        if (text.isBlank()) return
        val trimmed = text.trim()
        val kind = _classifyState.value.kind
        _classifyState.update {
            it.copy(isSubmitting = true, error = false, unreadable = false, sent = false, preview = null, cacheRef = null)
        }
        viewModelScope.launch {
            runCatching { repository.previewClassification(trimmed, country.code, kind) }
                .onSuccess { preview ->
                    _classifyState.update { it.copy(isSubmitting = false, preview = preview, pendingText = trimmed, cacheRef = null) }
                }
                .onFailure { t ->
                    val unreadable = t is UnreadablePasteException
                    _classifyState.update {
                        it.copy(isSubmitting = false, error = !unreadable, unreadable = unreadable)
                    }
                }
        }
    }

    /** Image intake — classify a SCREENSHOT/photo (vision). The preview's cacheRef confirms it later. */
    fun classifyImage(bytes: ByteArray, mime: String) {
        val kind = _classifyState.value.kind
        _classifyState.update {
            it.copy(isSubmitting = true, error = false, unreadable = false, sent = false, preview = null, pendingText = null, cacheRef = null)
        }
        viewModelScope.launch {
            runCatching { repository.previewClassificationImage(bytes, mime, country.code, kind) }
                .onSuccess { preview ->
                    _classifyState.update { it.copy(isSubmitting = false, preview = preview, cacheRef = preview.cacheRef, pendingText = null) }
                }
                .onFailure { t ->
                    val unreadable = t is UnreadablePasteException
                    _classifyState.update {
                        it.copy(isSubmitting = false, error = !unreadable, unreadable = unreadable)
                    }
                }
        }
    }

    /** Step 2 — confirm: the poster approved the preview → queue it as a moderated (pending) post. */
    fun confirmClassified() {
        val s = _classifyState.value
        val cacheRef = s.cacheRef
        val text = s.pendingText
        if (cacheRef == null && text == null) return
        val kind = s.kind
        _classifyState.update { it.copy(isConfirming = true, error = false, unreadable = false) }
        viewModelScope.launch {
            runCatching {
                if (cacheRef != null) repository.confirmClassificationImage(cacheRef, country.code, kind)
                else repository.confirmClassification(text!!, country.code, kind)
            }
                .onSuccess { _classifyState.update { it.copy(isConfirming = false, sent = true, preview = null) } }
                .onFailure { t ->
                    val unreadable = t is UnreadablePasteException
                    _classifyState.update {
                        it.copy(
                            isConfirming = false,
                            error = !unreadable,
                            unreadable = unreadable,
                            preview = if (unreadable) null else it.preview,
                        )
                    }
                }
        }
    }

    /** Discard the preview and go back to editing the pasted text. */
    fun editClassified() {
        _classifyState.update { it.copy(preview = null, error = false, unreadable = false) }
    }

    fun resetClassifyState() {
        _classifyState.update { ClassifyState() }
    }
}
