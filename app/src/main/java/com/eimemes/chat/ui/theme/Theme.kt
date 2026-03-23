package com.eimemes.chat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Dark theme colors (matching web app CSS vars) ──────────────────────
val DarkBackground    = Color(0xFF0D0D14)
val DarkSurface       = Color(0xFF16161F)
val DarkSurface2      = Color(0xFF1E1E2A)
val DarkBorder        = Color(0x26FFFFFF)
val DarkText1         = Color(0xEEFFFFFF)
val DarkText2         = Color(0x99FFFFFF)
val DarkText3         = Color(0x55FFFFFF)
val AccentBlue        = Color(0xFF0A84FF)
val AccentDim         = Color(0x1A0A84FF)
val UserBubble        = Color(0xFF2F2F2F)
val GradientStart     = Color(0xFF5E9CFF)
val GradientEnd       = Color(0xFFC96EFF)

// ── Light theme colors ─────────────────────────────────────────────────
val LightBackground   = Color(0xFFE9EAF2)
val LightSurface      = Color(0xFFFFFFFF)
val LightBorder       = Color(0x14000000)
val LightText1        = Color(0xE0000000)
val LightText2        = Color(0x8A000000)
val LightText3        = Color(0x52000000)

private val DarkColorScheme = darkColorScheme(
    primary          = AccentBlue,
    onPrimary        = Color.White,
    secondary        = AccentBlue,
    background       = DarkBackground,
    surface          = DarkSurface,
    surfaceVariant   = DarkSurface2,
    onBackground     = DarkText1,
    onSurface        = DarkText1,
    onSurfaceVariant = DarkText2,
    outline          = DarkBorder,
    outlineVariant   = DarkBorder,
    scrim            = Color(0x99000000)
)

private val LightColorScheme = lightColorScheme(
    primary          = AccentBlue,
    onPrimary        = Color.White,
    secondary        = AccentBlue,
    background       = LightBackground,
    surface          = LightSurface,
    surfaceVariant   = Color(0xFFF0F1F8),
    onBackground     = LightText1,
    onSurface        = LightText1,
    onSurfaceVariant = LightText2,
    outline          = LightBorder,
    outlineVariant   = LightBorder,
    scrim            = Color(0x99000000)
)

@Composable
fun EimemesChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography(),
        content     = content
    )
}
