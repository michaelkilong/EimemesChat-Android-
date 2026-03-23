package com.eimemes.chat.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eimemes.chat.ui.theme.AccentBlue
import com.eimemes.chat.ui.theme.AccentPurple

@OptIn(ExperimentalTextApi::class)
@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue  = 1.0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F12)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "✦",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentBlue, AccentPurple),
                        start  = Offset(0f, 0f),
                        end    = Offset(100f, 0f)
                    ),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.alpha(alpha)
            )
            Text(
                "EimemesChat AI",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentBlue, AccentPurple),
                        start  = Offset(0f, 0f),
                        end    = Offset(400f, 0f)
                    ),
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}
