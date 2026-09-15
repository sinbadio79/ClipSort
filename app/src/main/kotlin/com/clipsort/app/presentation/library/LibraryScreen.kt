package com.clipsort.app.presentation.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clipsort.app.domain.model.CategoryWithCount

@Composable
fun LibraryScreen(
    onCategoryClick: (categoryId: Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(uiState.categories, key = { it.category.id }) { item ->
            CategoryRow(item = item, onClick = { onCategoryClick(item.category.id) })
        }
    }
}

@Composable
private fun CategoryRow(item: CategoryWithCount, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = item.category.name)
            Text(text = item.clipCount.toString())
        }
    }
}
