package com.clipsort.app.presentation.library

import com.clipsort.app.domain.model.CategoryWithCount

data class LibraryUiState(
    val categories: List<CategoryWithCount> = emptyList(),
    val isLoading: Boolean = true
)
