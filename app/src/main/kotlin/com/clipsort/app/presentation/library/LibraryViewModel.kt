package com.clipsort.app.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipsort.app.domain.model.CategoryWithCount
import com.clipsort.app.domain.repository.ClipRepository
import com.clipsort.app.domain.usecase.CreateCategoryUseCase
import com.clipsort.app.domain.usecase.DeleteCategoryUseCase
import com.clipsort.app.domain.usecase.GetCategoriesWithCountUseCase
import com.clipsort.app.domain.usecase.RenameCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    getCategoriesWithCountUseCase: GetCategoriesWithCountUseCase,
    clipRepository: ClipRepository,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val renameCategoryUseCase: RenameCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(getCategoriesWithCountUseCase(), clipRepository.observeAllClips()) { categories, clips ->
                categories to clips
            }.catch { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }.collect { (categories, clips) ->
                _uiState.update { it.copy(categories = categories, clips = clips, isLoading = false) }
            }
        }
    }

    // --- Création ---

    fun onAddCategoryClicked() {
        _uiState.update { it.copy(isCreateDialogOpen = true, newCategoryName = "") }
    }

    fun onNewCategoryNameChanged(name: String) {
        _uiState.update { it.copy(newCategoryName = name) }
    }

    fun onDismissCreateDialog() {
        _uiState.update { it.copy(isCreateDialogOpen = false, newCategoryName = "") }
    }

    fun onConfirmCreateCategory() {
        val name = _uiState.value.newCategoryName
        viewModelScope.launch {
            createCategoryUseCase(name)
                .onSuccess { _uiState.update { it.copy(isCreateDialogOpen = false, newCategoryName = "") } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
        }
    }

    // --- Renommage ---

    fun onRenameRequested(item: CategoryWithCount) {
        _uiState.update { it.copy(categoryPendingRename = item, renameText = item.category.name) }
    }

    fun onRenameTextChanged(text: String) {
        _uiState.update { it.copy(renameText = text) }
    }

    fun onDismissRenameDialog() {
        _uiState.update { it.copy(categoryPendingRename = null, renameText = "") }
    }

    fun onConfirmRename() {
        val target = _uiState.value.categoryPendingRename ?: return
        val newName = _uiState.value.renameText
        viewModelScope.launch {
            renameCategoryUseCase(target.category.id, newName)
                .onSuccess { _uiState.update { it.copy(categoryPendingRename = null, renameText = "") } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
        }
    }

    // --- Suppression ---

    fun onDeleteRequested(item: CategoryWithCount) {
        _uiState.update { it.copy(categoryPendingDeletion = item) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(categoryPendingDeletion = null) }
    }

    fun onConfirmDelete() {
        val target = _uiState.value.categoryPendingDeletion ?: return
        viewModelScope.launch {
            deleteCategoryUseCase(target.category.id)
                .onSuccess { _uiState.update { it.copy(categoryPendingDeletion = null, categoryPendingRename = null) } }
                .onFailure { error -> _uiState.update { it.copy(errorMessage = error.message) } }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
