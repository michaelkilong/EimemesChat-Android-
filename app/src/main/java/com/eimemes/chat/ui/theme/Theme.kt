package com.eimemes.chat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary          = Color(0xFF4FA8FF),
    onPrimary        = Color(0xFF000000),
    primaryContainer = Color(0xFF1A3A6E),
    background       = Color(0xFF141417),
    surface          = Color(0xFF1C1C24),
    surfaceVariant   = Color(0xFF2C2C38),
    onBackground     = Color(0xF0FFFFFF),
    onSurface        = Color(0xF0FFFFFF),
    onSurfaceVariant = Color(0xFF8888AA),
    outline          = Color(0xFF2A2A3A),
    error            = Color(0xFFFF6B6B),
)

private val LightColors = lightColorScheme(
    primary          = Color(0xFF0A84FF),
    onPrimary        = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD0E4FF),
    background       = Color(0xFFE9EAF2),
    surface          = Color(0xFFFFFFFF),
    surfaceVariant   = Color(0xFFF0F0F8),
    onBackground     = Color(0xE0000000),
    onSurface        = Color(0xE0000000),
    onSurfaceVariant = Color(0xFF666688),
    outline          = Color(0xFFDDDDEE),
    error            = Color(0xFFCC0000),
)

val AccentBlue   = Color(0xFF4FA8FF)
val AccentPurple = Color(0xFFC96EFF)

@Composable
fun EimemesChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
