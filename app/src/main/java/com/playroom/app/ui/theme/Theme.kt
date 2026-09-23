package com.playroom.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ----- PlayRoom palette (dark gaming theme) -----
val NeonGreen = Color(0xFF39FF14)      // primary accent
val NeonPurple = Color(0xFF7C4DFF)     // secondary accent
val DeepBackground = Color(0xFF0B0B14) // app background
val CardBackground = Color(0xFF16162A) // cards
val TextPrimary = Color(0xFFECECF3)    // main text
val TextSecondary = Color(0xFF9A9AB0)  // secondary text
val DangerRed = Color(0xFFFF4757)      // errors / disconnected

private val PlayRoomColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DeepBackground,
    secondary = NeonPurple,
    onSecondary = Color.White,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    error = DangerRed
)

private val PlayRoomTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.Black, fontSize = 34.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
)

@Composable
fun PlayRoomTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PlayRoomColorScheme,
        typography = PlayRoomTypography,
        content = content
    )
}
