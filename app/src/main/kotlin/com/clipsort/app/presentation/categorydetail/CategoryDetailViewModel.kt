package com.clipsort.app.presentation.categorydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.repository.ClipRepository
import com.clipsort.app.domain.usecase.GetCategoryByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryDetailUiState(
    val categoryName: String = "",
    val clips: List<Clip> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clipRepository: ClipRepository,
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase
) : ViewModel() {

    private val categoryId: Long? = savedStateHandle["categoryId"]

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val category = categoryId?.let { getCategoryByIdUseCase(it) }
            _uiState.update { it.copy(categoryName = category?.name.orEmpty()) }
        }
        viewModelScope.launch {
            val clipsFlow = categoryId?.let { clipRepository.observeClipsByCategory(it) } ?: clipRepository.observeAllClips()
            clipsFlow.collect { clips ->
                _uiState.update { it.copy(clips = clips, isLoading = false) }
            }
        }
    }

    fun onToggleStatus(clip: Clip) {
        val newStatus = if (clip.status == ClipStatus.WATCHED) ClipStatus.TO_WATCH else ClipStatus.WATCHED
        viewModelScope.launch {
            clipRepository.updateStatus(clip.id, newStatus)
        }
    }

    fun onDeleteClip(clip: Clip) {
        viewModelScope.launch {
            clipRepository.deleteClip(clip.id)
        }
    }
}
