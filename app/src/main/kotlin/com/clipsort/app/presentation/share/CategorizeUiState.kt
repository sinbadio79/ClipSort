package com.clipsort.app.presentation.share

import com.clipsort.app.domain.model.Category
import com.clipsort.app.domain.model.SourceApp

/**
 * État immuable de l'écran de catégorisation. Une seule source de vérité,
 * exposée par le ViewModel via StateFlow — pas de état dispersé dans des `var` de Compose.
 */
data class CategorizeUiState(
    val sharedUrl: String = "",
    val detectedSource: SourceApp = SourceApp.UNKNOWN,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val comment: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
