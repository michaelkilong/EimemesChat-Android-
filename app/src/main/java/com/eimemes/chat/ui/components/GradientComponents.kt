package com.eimemes.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.eimemes.chat.ui.theme.AccentBlue
import com.eimemes.chat.ui.theme.AccentPurple

val brandGradient = Brush.linearGradient(listOf(AccentBlue, AccentPurple))

@Composable
fun GradientBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier.background(brandGradient)) { content() }
}
