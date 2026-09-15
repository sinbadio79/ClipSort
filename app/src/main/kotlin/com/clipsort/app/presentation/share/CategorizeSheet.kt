package com.clipsort.app.presentation.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipsort.app.domain.model.Category

/**
 * Bottom sheet affichée au-dessus de l'app source lors d'un partage.
 * Composant "pauvre" : toute la logique vit dans le ViewModel, ce composable ne fait
 * qu'observer l'état et remonter les intentions utilisateur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizeSheet(
    sharedText: String,
    onDismiss: () -> Unit,
    viewModel: CategorizeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sharedText) {
        viewModel.onSharedTextReceived(sharedText)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onDismiss()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Ajouter à ClipSort")

            CategoryChipRow(
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = viewModel::onCategorySelected,
                onCreateCategory = viewModel::onCreateCategory
            )

            OutlinedTextField(
                value = uiState.comment,
                onValueChange = viewModel::onCommentChanged,
                label = { Text("Petit commentaire (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { message ->
                Text(text = message)
            }

            Button(
                onClick = viewModel::onSaveClicked,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "Enregistrement…" else "Enregistrer")
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    onCreateCategory: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.name) }
            )
        }
        item {
            FilterChip(
                selected = false,
                onClick = { onCreateCategory("Nouvelle catégorie") },
                label = { Text("+ Nouvelle") }
            )
        }
    }
}
