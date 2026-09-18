package com.clipsort.app.presentation.categorydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipsort.app.R
import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import com.clipsort.app.domain.repository.CategoryRepository
import com.clipsort.app.domain.repository.ClipRepository
import com.clipsort.app.domain.usecase.ClipFilter
import com.clipsort.app.domain.usecase.ClipOrder
import com.clipsort.app.domain.usecase.FilterClipsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryDetailUiState(
    val categoryName: String = "",
    val isCollection: Boolean = false,
    val categories: List<Category> = emptyList(),
    val clips: List<Clip> = emptyList(),
    val visibleClips: List<Clip> = emptyList(),
    val filter: ClipFilter = ClipFilter(),
    val isLoading: Boolean = true,
    val isWorking: Boolean = false,
    val pendingDeletion: Clip? = null,
    val pendingEdit: Clip? = null,
    val noteDraft: String = "",
    val errorMessage: Int? = null
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val clipRepository: ClipRepository,
    categoryRepository: CategoryRepository,
    private val filterClips: FilterClipsUseCase
) : ViewModel() {
    private val categoryId: Long? = savedStateHandle["categoryId"]
    private val initialFilter = ClipFilter(
        query = savedStateHandle["query"] ?: "",
        source = savedStateHandle.get<String>("source")?.let { value -> SourceApp.entries.find { it.name == value } },
        status = savedStateHandle.get<String>("status")?.let { value -> ClipStatus.entries.find { it.name == value } },
        order = savedStateHandle.get<String>("order")?.let { value -> ClipOrder.entries.find { it.name == value } } ?: ClipOrder.NEWEST
    )
    private val _uiState = MutableStateFlow(CategoryDetailUiState(isCollection = categoryId != null, filter = initialFilter))
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val clips = categoryId?.let { clipRepository.observeClipsByCategory(it) } ?: clipRepository.observeAllClips()
            combine(clips, categoryRepository.observeCategories()) { values, categories -> values to categories }
                .catch { _uiState.update { it.copy(isLoading = false, errorMessage = R.string.action_failed) } }
                .collect { (values, categories) ->
                    _uiState.update {
                        it.copy(clips = values, categories = categories,
                            categoryName = categories.find { category -> category.id == categoryId }?.name.orEmpty(),
                            visibleClips = filterClips(values, categories, it.filter), isLoading = false)
                    }
                }
        }
    }

    fun onQueryChanged(query: String) = updateFilter { it.copy(query = query) }
    fun onFilterChanged(filter: ClipFilter) = updateFilter { filter }
    fun onSourceChanged(source: SourceApp?) = updateFilter { it.copy(source = source) }
    fun onStatusChanged(status: ClipStatus?) = updateFilter { it.copy(status = status) }
    fun onToggleOrder() = updateFilter { it.copy(order = if (it.order == ClipOrder.NEWEST) ClipOrder.OLDEST else ClipOrder.NEWEST) }
    fun onResetFilters() = updateFilter { ClipFilter() }

    private fun updateFilter(change: (ClipFilter) -> ClipFilter) {
        _uiState.update {
            val filter = change(it.filter)
            it.copy(filter = filter, visibleClips = filterClips(it.clips, it.categories, filter))
        }
        val filter = _uiState.value.filter
        savedStateHandle["query"] = filter.query
        savedStateHandle["source"] = filter.source?.name
        savedStateHandle["status"] = filter.status?.name
        savedStateHandle["order"] = filter.order.name
    }

    fun onToggleStatus(clip: Clip) = mutate {
        clipRepository.updateStatus(clip.id, if (clip.status == ClipStatus.WATCHED) ClipStatus.TO_WATCH else ClipStatus.WATCHED)
    }
    fun onDeleteRequested(clip: Clip) { if (!_uiState.value.isWorking) _uiState.update { it.copy(pendingDeletion = clip) } }
    fun onDismissDelete() { if (!_uiState.value.isWorking) _uiState.update { it.copy(pendingDeletion = null) } }
    fun onConfirmDelete() {
        val clip = _uiState.value.pendingDeletion ?: return
        mutate { clipRepository.deleteClip(clip.id) }
    }
    fun onEditRequested(clip: Clip) { if (!_uiState.value.isWorking) _uiState.update { it.copy(pendingEdit = clip, noteDraft = clip.comment.orEmpty()) } }
    fun onNoteChanged(note: String) { _uiState.update { it.copy(noteDraft = note) } }
    fun onDismissEdit() { if (!_uiState.value.isWorking) _uiState.update { it.copy(pendingEdit = null) } }
    fun onConfirmEdit() {
        val state = _uiState.value
        val clip = state.pendingEdit ?: return
        mutate { clipRepository.updateComment(clip.id, state.noteDraft.trim().takeIf { it.isNotEmpty() }) }
    }
    fun onErrorDismissed() { _uiState.update { it.copy(errorMessage = null) } }

    private fun mutate(operation: suspend () -> Unit) {
        if (_uiState.value.isWorking) return
        _uiState.update { it.copy(isWorking = true) }
        viewModelScope.launch {
            try {
                operation()
                _uiState.update { it.copy(pendingDeletion = null, pendingEdit = null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = R.string.action_failed) }
            } finally {
                _uiState.update { it.copy(isWorking = false) }
            }
        }
    }
}
