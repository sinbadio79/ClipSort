package com.clipsort.app.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.clipsort.app.R
import com.clipsort.app.domain.model.SourceApp

@Composable
fun sourceLabel(source: SourceApp): String = when (source) {
    SourceApp.YOUTUBE -> "YouTube"
    SourceApp.INSTAGRAM -> "Instagram"
    SourceApp.TIKTOK -> "TikTok"
    SourceApp.FACEBOOK -> "Facebook"
    SourceApp.UNKNOWN -> stringResource(R.string.other_source)
}

/** A platform marker, deliberately not a fabricated video thumbnail. */
@Composable
fun SourceTile(source: SourceApp, modifier: Modifier = Modifier) {
    val color = when (source) {
        SourceApp.YOUTUBE -> Color(0xFFAA4B39)
        SourceApp.INSTAGRAM -> Color(0xFF795287)
        SourceApp.TIKTOK -> Color(0xFF285D55)
        SourceApp.FACEBOOK -> Color(0xFF365F8A)
        SourceApp.UNKNOWN -> Color(0xFF55635A)
    }
    Box(modifier.clip(MaterialTheme.shapes.medium).background(color), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = .12f), size.width * .6f, Offset(size.width, 0f))
            drawCircle(Color.White.copy(alpha = .08f), size.width * .6f, Offset(0f, size.height))
        }
        Box(Modifier.size(38.dp).background(Color.White.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PlayArrow, null, Modifier.size(25.dp), tint = Color.White)
        }
    }
}
