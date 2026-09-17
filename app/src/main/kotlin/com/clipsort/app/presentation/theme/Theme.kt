package com.clipsort.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF20584D), onPrimary = Color.White,
    primaryContainer = Color(0xFFDCECE3), onPrimaryContainer = Color(0xFF153C34),
    secondary = Color(0xFF99513C), secondaryContainer = Color(0xFFFFE3D5),
    onSecondaryContainer = Color(0xFF5D2B1C),
    background = Color(0xFFF7F6F0), onBackground = Color(0xFF202D28),
    surface = Color(0xFFFFFEFA), onSurface = Color(0xFF202D28),
    surfaceVariant = Color(0xFFECEEE6), onSurfaceVariant = Color(0xFF566159),
    outlineVariant = Color(0xFFDDE2D8)
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA3D5C2), onPrimary = Color(0xFF10382C),
    primaryContainer = Color(0xFF254C3F), onPrimaryContainer = Color(0xFFD1EBDD),
    secondary = Color(0xFFF3BCA3), secondaryContainer = Color(0xFF613B2D),
    onSecondaryContainer = Color(0xFFFFDDCC),
    background = Color(0xFF151D19), onBackground = Color(0xFFE6EDE5),
    surface = Color(0xFF1D2822), onSurface = Color(0xFFE6EDE5),
    surfaceVariant = Color(0xFF303D34), onSurfaceVariant = Color(0xFFBAC8BA),
    outlineVariant = Color(0xFF435148)
)
private val ClipTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 34.sp, lineHeight = 39.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
)

@Composable
fun ClipSortTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ClipTypography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content
    )
}
