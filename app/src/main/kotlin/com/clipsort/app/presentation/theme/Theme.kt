package com.clipsort.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ClipSortColorScheme = lightColorScheme()

@Composable
fun ClipSortTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ClipSortColorScheme,
        content = content
    )
}
