package com.clipsort.app.presentation.library

import com.clipsort.app.domain.model.CategoryWithCount
import com.clipsort.app.domain.model.Clip

data class LibraryUiState(
    val categories: List<CategoryWithCount> = emptyList(),
    val clips: List<Clip> = emptyList(),
    val isLoading: Boolean = true,
    val isCreateDialogOpen: Boolean = false,
    val newCategoryName: String = "",
    val categoryPendingDeletion: CategoryWithCount? = null,
    val categoryPendingRename: CategoryWithCount? = null,
    val renameText: String = "",
    val errorMessage: String? = null
)
