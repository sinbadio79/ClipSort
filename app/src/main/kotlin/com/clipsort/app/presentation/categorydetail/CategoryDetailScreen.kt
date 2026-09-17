package com.clipsort.app.presentation.categorydetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clipsort.app.R
import com.clipsort.app.domain.model.Clip
import com.clipsort.app.domain.model.ClipStatus
import com.clipsort.app.domain.model.SourceApp
import com.clipsort.app.domain.usecase.ClipFilter
import com.clipsort.app.domain.usecase.ClipOrder
import com.clipsort.app.presentation.common.*
import com.clipsort.app.presentation.library.LibraryNavigation
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

@Composable
fun CategoryDetailScreen(onBack: () -> Unit, onBrowseAll: () -> Unit = {}, viewModel: CategoryDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val openError = stringResource(R.string.open_failed)
    val shareTitle = stringResource(R.string.share_clip)
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbar.showSnackbar(context.getString(it)); viewModel.onErrorDismissed() }
    }
    CategoryDetailContent(state, onBack, onBrowseAll, viewModel::onFilterChanged,
        onOpenClip = { if (!openClipLink(context, it.url)) scope.launch { snackbar.showSnackbar(openError) } },
        onToggleStatus = viewModel::onToggleStatus,
        onShareClip = { if (!shareClipLink(context, it.url, shareTitle)) scope.launch { snackbar.showSnackbar(openError) } },
        onEditClip = viewModel::onEditRequested, onDeleteClip = viewModel::onDeleteRequested, snackbar = snackbar)

    if (state.pendingDeletion != null) {
        AlertDialog(onDismissRequest = viewModel::onDismissDelete,
            title = { Text(stringResource(R.string.delete_clip_title)) },
            text = { Text(stringResource(R.string.delete_clip_body)) },
            confirmButton = { TextButton(onClick = viewModel::onConfirmDelete, enabled = !state.isWorking) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = viewModel::onDismissDelete, enabled = !state.isWorking) { Text(stringResource(R.string.cancel)) } })
    }
    if (state.pendingEdit != null) {
        AlertDialog(onDismissRequest = viewModel::onDismissEdit,
            title = { Text(stringResource(R.string.edit_note)) },
            text = { OutlinedTextField(state.noteDraft, viewModel::onNoteChanged, label = { Text(stringResource(R.string.clip_note)) }, minLines = 3, maxLines = 6, enabled = !state.isWorking) },
            confirmButton = { TextButton(onClick = viewModel::onConfirmEdit, enabled = !state.isWorking) { Text(stringResource(R.string.confirm)) } },
            dismissButton = { TextButton(onClick = viewModel::onDismissEdit, enabled = !state.isWorking) { Text(stringResource(R.string.cancel)) } })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailContent(
    state: CategoryDetailUiState,
    onBack: () -> Unit,
    onBrowseAll: () -> Unit,
    onFilterChanged: (ClipFilter) -> Unit,
    onOpenClip: (Clip) -> Unit,
    onToggleStatus: (Clip) -> Unit,
    onShareClip: (Clip) -> Unit,
    onEditClip: (Clip) -> Unit,
    onDeleteClip: (Clip) -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() }
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.filter) { listState.scrollToItem(0) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.library_eyebrow), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(if (state.isCollection) state.categoryName else stringResource(R.string.all_clips),
                            style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = { if (state.isCollection) IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, stringResource(R.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = { LibraryNavigation(false, onBack, onBrowseAll) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(padding).testTag("clips-list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                OutlinedTextField(value = state.filter.query, onValueChange = { onFilterChanged(state.filter.copy(query = it)) },
                    modifier = Modifier.fillMaxWidth().testTag("clip-search"), singleLine = true, shape = MaterialTheme.shapes.medium,
                    label = { Text(stringResource(R.string.search_clips), style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = { if (state.filter.query.isNotEmpty()) IconButton(onClick = { onFilterChanged(state.filter.copy(query = "")) }) { Icon(Icons.Default.Close, stringResource(R.string.clear_search)) } })
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = state.filter.status == null, onClick = { onFilterChanged(state.filter.copy(status = null)) }, label = { Text(stringResource(R.string.all)) }, modifier = Modifier.testTag("status-all")) }
                    item { FilterChip(selected = state.filter.status == ClipStatus.TO_WATCH, onClick = { onFilterChanged(state.filter.copy(status = ClipStatus.TO_WATCH)) }, label = { Text(stringResource(R.string.to_watch)) }, modifier = Modifier.testTag("status-to-watch")) }
                    item { FilterChip(selected = state.filter.status == ClipStatus.WATCHED, onClick = { onFilterChanged(state.filter.copy(status = ClipStatus.WATCHED)) }, label = { Text(stringResource(R.string.watched)) }, modifier = Modifier.testTag("status-watched")) }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = state.filter.source == null, onClick = { onFilterChanged(state.filter.copy(source = null)) }, label = { Text(stringResource(R.string.all_sources)) }) }
                    items(SourceApp.entries) { source ->
                        FilterChip(selected = state.filter.source == source,
                            onClick = { onFilterChanged(state.filter.copy(source = if (state.filter.source == source) null else source)) },
                            label = { Text(sourceLabel(source)) }, modifier = Modifier.testTag("source-${source.name}"))
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(pluralStringResource(R.plurals.clip_count, state.visibleClips.size, state.visibleClips.size), style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { onFilterChanged(state.filter.copy(order = if (state.filter.order == ClipOrder.NEWEST) ClipOrder.OLDEST else ClipOrder.NEWEST)) }, modifier = Modifier.testTag("clip-order")) {
                        Text(stringResource(if (state.filter.order == ClipOrder.NEWEST) R.string.newest_first else R.string.oldest_first))
                    }
                }
            }
            if (state.isLoading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            } else if (state.visibleClips.isEmpty()) {
                item {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.fillMaxWidth().padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(Icons.Default.Search, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.empty_results_title), style = MaterialTheme.typography.headlineMedium)
                            Text(stringResource(if (state.clips.isEmpty()) R.string.empty_collection_body else R.string.empty_results_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (state.filter != ClipFilter()) {
                                FilledTonalButton(onClick = { onFilterChanged(ClipFilter()) }, modifier = Modifier.testTag("reset-filters")) { Text(stringResource(R.string.clear_filters)) }
                            }
                        }
                    }
                }
            } else {
                items(state.visibleClips, key = { it.id }) { clip ->
                    ClipCard(clip, state.categories.find { it.id == clip.categoryId }?.name.orEmpty(), state.isWorking,
                        { onOpenClip(clip) }, { onToggleStatus(clip) }, { onShareClip(clip) }, { onEditClip(clip) }, { onDeleteClip(clip) })
                }
            }
        }
    }
}

@Composable
private fun ClipCard(clip: Clip, categoryName: String, busy: Boolean, onOpen: () -> Unit, onToggle: () -> Unit, onShare: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]
    val date = remember(clip.createdAt, locale) { DateFormat.getDateInstance(DateFormat.SHORT, locale).format(Date(clip.createdAt)) }
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth().testTag("clip-${clip.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                SourceTile(clip.sourceApp, Modifier.width(78.dp).height(96.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(sourceLabel(clip.sourceApp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(clip.comment?.takeIf { it.isNotBlank() } ?: stringResource(R.string.clip_fallback_title), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(categoryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onToggle, enabled = !busy, modifier = Modifier.weight(1f).testTag("toggle-${clip.id}"), contentPadding = PaddingValues(horizontal = 0.dp)) {
                    if (clip.status == ClipStatus.WATCHED) { Icon(Icons.Default.Check, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)) }
                    Text(stringResource(if (clip.status == ClipStatus.WATCHED) R.string.mark_unwatched else R.string.mark_watched), maxLines = 2)
                }
                IconButton(onClick = onShare, modifier = Modifier.testTag("share-${clip.id}")) { Icon(Icons.Default.Share, stringResource(R.string.share_clip)) }
                Box {
                    IconButton(onClick = { showMenu = true }, enabled = !busy, modifier = Modifier.testTag("options-${clip.id}")) { Icon(Icons.Default.MoreVert, stringResource(R.string.clip_options)) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.edit_note)) }, onClick = { showMenu = false; onEdit() })
                        DropdownMenuItem(text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onDelete() })
                    }
                }
            }
        }
    }
}
