package com.clipsort.app.presentation.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipsort.app.domain.model.CategoryWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onCategoryClick: (categoryId: Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("ClipSort") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::onAddCategoryClicked) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nouvelle catégorie")
            }
        }
    ) { padding ->
        if (uiState.categories.isEmpty() && !uiState.isLoading) {
            EmptyLibrary(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(padding)) {
                items(uiState.categories, key = { it.category.id }) { item ->
                    CategoryRow(
                        item = item,
                        onClick = { onCategoryClick(item.category.id) },
                        onLongClick = { viewModel.onRenameRequested(item) }
                    )
                }
            }
        }
    }

    if (uiState.isCreateDialogOpen) {
        CategoryNameDialog(
            title = "Nouvelle catégorie",
            name = uiState.newCategoryName,
            onNameChanged = viewModel::onNewCategoryNameChanged,
            onConfirm = viewModel::onConfirmCreateCategory,
            onDismiss = viewModel::onDismissCreateDialog
        )
    }

    uiState.categoryPendingRename?.let {
        CategoryNameDialog(
            title = "Renommer la catégorie",
            name = uiState.renameText,
            onNameChanged = viewModel::onRenameTextChanged,
            onConfirm = viewModel::onConfirmRename,
            onDismiss = viewModel::onDismissRenameDialog,
            extraAction = { TextButton(onClick = { viewModel.onDeleteRequested(it) }) { Text("Supprimer") } }
        )
    }

    uiState.categoryPendingDeletion?.let { pending ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            title = { Text("Supprimer « ${pending.category.name} » ?") },
            text = { Text("Les ${pending.clipCount} clip(s) de cette catégorie seront supprimés définitivement.") },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmDelete) { Text("Supprimer") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDeleteDialog) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun EmptyLibrary(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Ta bibliothèque est vide",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Partage un reel depuis TikTok, Instagram ou YouTube pour commencer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(
    item: CategoryWithCount,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(runCatching { Color(android.graphics.Color.parseColor(item.category.colorHex)) }.getOrDefault(Color.Gray))
            )
            Text(
                text = item.category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = item.clipCount.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    name: String,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    extraAction: (@Composable () -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                singleLine = true,
                label = { Text("Nom") }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) { Text("Valider") }
        },
        dismissButton = {
            Row {
                extraAction?.invoke()
                TextButton(onClick = onDismiss) { Text("Annuler") }
            }
        }
    )
}
