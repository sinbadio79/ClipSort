package com.clipsort.app.presentation.share

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clipsort.app.R
import com.clipsort.app.presentation.common.SourceTile
import com.clipsort.app.presentation.common.sharedHttpUrl
import com.clipsort.app.presentation.common.sourceLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizeSheet(
    sharedText: String,
    onDismiss: () -> Unit,
    viewModel: CategorizeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val url = remember(sharedText) { sharedHttpUrl(sharedText).orEmpty() }
    LaunchedEffect(url) { viewModel.onSharedTextReceived(url) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onDismiss() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CategorizeContent(
            state = state,
            onCategorySelected = viewModel::onCategorySelected,
            onCommentChanged = viewModel::onCommentChanged,
            onCreateCategory = viewModel::onCreateCategoryClicked,
            onCategoryNameChanged = viewModel::onNewCategoryNameChanged,
            onConfirmCategory = viewModel::onConfirmCreateCategory,
            onCancelCategory = viewModel::onCancelCreateCategory,
            onSave = viewModel::onSaveClicked
        )
    }
}

/** Stateless share surface: reusable in instrumented tests without fake application data. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorizeContent(
    state: CategorizeUiState,
    onCategorySelected: (Long) -> Unit,
    onCommentChanged: (String) -> Unit,
    onCreateCategory: () -> Unit,
    onCategoryNameChanged: (String) -> Unit,
    onConfirmCategory: () -> Unit,
    onCancelCategory: () -> Unit,
    onSave: () -> Unit
) {
    val busy = state.isSaving || state.isSubmittingCategory
    Column(
        Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp).testTag("share-content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(stringResource(R.string.add_title), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.add_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                SourceTile(state.detectedSource, Modifier.size(64.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(sourceLabel(state.detectedSource), style = MaterialTheme.typography.titleMedium)
                    Text(
                        state.sharedUrl.ifBlank { stringResource(R.string.invalid_shared_link) },
                        Modifier.testTag("shared-url"), maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Text(stringResource(R.string.choose_collection), style = MaterialTheme.typography.titleMedium)
        if (state.isCreatingCategory) {
            OutlinedTextField(
                state.newCategoryName, onCategoryNameChanged,
                Modifier.fillMaxWidth().testTag("share-category-name"), enabled = !busy,
                label = { Text(stringResource(R.string.collection_name)) }, singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onCancelCategory, enabled = !busy) { Text(stringResource(R.string.cancel)) }
                Button(onConfirmCategory, enabled = !busy && state.newCategoryName.isNotBlank()) {
                    Text(stringResource(if (state.isSubmittingCategory) R.string.saving else R.string.create))
                }
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.categories.forEach { category ->
                    FilterChip(
                        selected = category.id == state.selectedCategoryId,
                        onClick = { onCategorySelected(category.id) }, enabled = !busy,
                        label = { Text(category.name) }, modifier = Modifier.testTag("share-category-${category.id}")
                    )
                }
                AssistChip(
                    onClick = onCreateCategory, enabled = !busy,
                    label = { Text(stringResource(R.string.new_collection)) },
                    leadingIcon = { Icon(Icons.Default.Add, null) },
                    modifier = Modifier.testTag("share-new-category")
                )
            }
        }
        OutlinedTextField(
            state.comment, onCommentChanged, Modifier.fillMaxWidth().testTag("share-note"),
            enabled = !busy, label = { Text(stringResource(R.string.clip_note)) },
            placeholder = { Text(stringResource(R.string.clip_note_hint)) }, minLines = 2, maxLines = 4
        )
        if (state.errorMessage != null) {
            Text(stringResource(R.string.action_failed), color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = onSave,
            enabled = !busy && !state.isSaved && !state.isCreatingCategory && state.selectedCategoryId != null && state.sharedUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("share-save")
        ) { Text(stringResource(if (state.isSaving) R.string.saving else R.string.save_clip)) }
        Spacer(Modifier.height(12.dp))
    }
}
