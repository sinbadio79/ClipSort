package com.clipsort.app.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clipsort.app.R
import com.clipsort.app.domain.model.CategoryWithCount
import com.clipsort.app.domain.model.ClipStatus

@Composable
fun LibraryScreen(onCategoryClick: (Long) -> Unit, onBrowseClips: () -> Unit, viewModel: LibraryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(it); viewModel.onErrorDismissed() }
    }
    LibraryContent(state, onCategoryClick, onBrowseClips, viewModel::onAddCategoryClicked, viewModel::onRenameRequested, snackbar)
    if (state.isCreateDialogOpen) {
        CategoryNameDialog(stringResource(R.string.new_collection), state.newCategoryName,
            viewModel::onNewCategoryNameChanged, viewModel::onConfirmCreateCategory, viewModel::onDismissCreateDialog)
    }
    state.categoryPendingRename?.let { item ->
        CategoryNameDialog(stringResource(R.string.rename_collection), state.renameText,
            viewModel::onRenameTextChanged, viewModel::onConfirmRename, viewModel::onDismissRenameDialog,
            extraAction = { TextButton(onClick = { viewModel.onDeleteRequested(item) }) { Text(stringResource(R.string.delete)) } })
    }
    state.categoryPendingDeletion?.let { item ->
        AlertDialog(onDismissRequest = viewModel::onDismissDeleteDialog,
            title = { Text(stringResource(R.string.delete_collection_title, item.category.name)) },
            text = { Text(stringResource(R.string.delete_collection_body, item.clipCount)) },
            confirmButton = { TextButton(onClick = viewModel::onConfirmDelete) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = viewModel::onDismissDeleteDialog) { Text(stringResource(R.string.cancel)) } })
    }
}

@Composable
fun LibraryContent(
    state: LibraryUiState,
    onCategoryClick: (Long) -> Unit,
    onBrowseClips: () -> Unit,
    onAddCategory: () -> Unit,
    onManageCategory: (CategoryWithCount) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { LibraryNavigation(true, {}, onBrowseClips) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCategory, modifier = Modifier.testTag("add-collection")) {
                Icon(Icons.Default.Add, stringResource(R.string.new_collection))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("home-list"),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.padding(8.dp).size(24.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text("ClipSort", style = MaterialTheme.typography.titleLarge)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.personal_library), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.home_title), style = MaterialTheme.typography.headlineLarge)
                    Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (state.isLoading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth().testTag("library-loading")) }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Statistic(state.clips.size, stringResource(R.string.saved_clips), Modifier.weight(1f), false)
                        Statistic(state.clips.count { it.status == ClipStatus.TO_WATCH }, stringResource(R.string.to_watch), Modifier.weight(1f), true)
                    }
                }
                if (state.clips.isNotEmpty()) {
                    item {
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(stringResource(R.string.continue_title), style = MaterialTheme.typography.headlineMedium)
                                Text(stringResource(R.string.continue_subtitle), style = MaterialTheme.typography.bodyMedium)
                                Button(onClick = onBrowseClips, modifier = Modifier.testTag("browse-clips")) {
                                    Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.browse_clips))
                                }
                            }
                        }
                    }
                }
                item { Text(stringResource(R.string.collections), style = MaterialTheme.typography.titleLarge) }
                if (state.categories.isEmpty()) {
                    item {
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(stringResource(R.string.empty_home_title), style = MaterialTheme.typography.headlineMedium)
                                Text(stringResource(R.string.empty_home_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                FilledTonalButton(onClick = onAddCategory) { Text(stringResource(R.string.create_collection)) }
                            }
                        }
                    }
                } else {
                    state.categories.chunked(2).forEach { row ->
                        item(key = "categories-${row.first().category.id}") {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { category ->
                                    CollectionCard(category, { onCategoryClick(category.category.id) }, { onManageCategory(category) }, Modifier.weight(1f))
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                item { Text(stringResource(R.string.share_tip), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun Statistic(value: Int, label: String, modifier: Modifier, accent: Boolean) {
    Surface(modifier, shape = MaterialTheme.shapes.medium,
        color = if (accent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CollectionCard(item: CategoryWithCount, onOpen: () -> Unit, onManage: () -> Unit, modifier: Modifier) {
    Card(onClick = onOpen, modifier = modifier.testTag("collection-${item.category.id}"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val color = runCatching { Color(android.graphics.Color.parseColor(item.category.colorHex)) }.getOrDefault(MaterialTheme.colorScheme.primary)
                Box(Modifier.size(30.dp).background(color.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(10.dp).background(color, CircleShape))
                }
                IconButton(onClick = onManage, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.manage_collection, item.category.name))
                }
            }
            Text(item.category.name, style = MaterialTheme.typography.titleMedium)
            Text(pluralStringResource(R.plurals.clip_count, item.clipCount, item.clipCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LibraryNavigation(homeSelected: Boolean, onHome: () -> Unit, onClips: () -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(selected = homeSelected, onClick = onHome, icon = { Icon(Icons.Default.Home, null) }, label = { Text(stringResource(R.string.home)) }, modifier = Modifier.testTag("nav-home"))
        NavigationBarItem(selected = !homeSelected, onClick = onClips, icon = { Icon(Icons.Default.PlayArrow, null) }, label = { Text(stringResource(R.string.my_clips)) }, modifier = Modifier.testTag("nav-clips"))
    }
}

@Composable
private fun CategoryNameDialog(title: String, name: String, onNameChanged: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit, extraAction: (@Composable () -> Unit)? = null) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = onNameChanged, singleLine = true, label = { Text(stringResource(R.string.collection_name)) }) },
        confirmButton = { TextButton(onClick = onConfirm, enabled = name.isNotBlank()) { Text(stringResource(R.string.confirm)) } },
        dismissButton = { Row { extraAction?.invoke(); TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } } })
}
