package com.clipsort.app.presentation.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipsort.app.domain.repository.CategoryRepository
import com.clipsort.app.domain.usecase.CreateCategoryUseCase
import com.clipsort.app.domain.usecase.DetectSourceAppUseCase
import com.clipsort.app.domain.usecase.SaveClipUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Pilote l'écran affiché au moment du partage (bottom sheet de catégorisation).
 * Ne connaît que des modèles domain et des use cases — jamais Room, jamais Android Context.
 */
@HiltViewModel
class CategorizeViewModel @Inject constructor(
    private val detectSourceAppUseCase: DetectSourceAppUseCase,
    private val saveClipUseCase: SaveClipUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategorizeUiState())
    val uiState: StateFlow<CategorizeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onSharedTextReceived(sharedText: String) {
        val detectedSource = detectSourceAppUseCase(sharedText)
        _uiState.update { it.copy(sharedUrl = sharedText.trim(), detectedSource = detectedSource) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun onCommentChanged(comment: String) {
        _uiState.update { it.copy(comment = comment) }
    }

    fun onCreateCategory(name: String) {
        viewModelScope.launch {
            createCategoryUseCase(name)
                .onSuccess { category -> onCategorySelected(category.id) }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
        }
    }

    fun onSaveClicked() {
        val currentState = _uiState.value
        val categoryId = currentState.selectedCategoryId

        if (categoryId == null) {
            _uiState.update { it.copy(errorMessage = "Choisis une catégorie avant d'enregistrer.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            saveClipUseCase(
                url = currentState.sharedUrl,
                sourceApp = currentState.detectedSource,
                categoryId = categoryId,
                comment = currentState.comment
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
            }
        }
    }
}
