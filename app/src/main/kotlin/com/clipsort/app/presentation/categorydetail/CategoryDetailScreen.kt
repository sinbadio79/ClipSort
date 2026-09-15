package com.clipsort.app.presentation.categorydetail

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipsort.app.domain.model.Clip

@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(uiState.clips, key = { it.id }) { clip ->
            ClipCard(
                clip = clip,
                onToggleStatus = { viewModel.onToggleStatus(clip) },
                onDelete = { viewModel.onDeleteClip(clip) }
            )
        }
    }
}

@Composable
private fun ClipCard(
    clip: Clip,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.padding(4.dp)) {
        Text(text = clip.sourceApp.name)
        clip.comment?.let { comment -> Text(text = comment) }
    }
}
