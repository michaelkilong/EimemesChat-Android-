package com.eimemes.chat.ui.about

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.foundation.shape.CircleShape
import com.eimemes.chat.ui.theme.AccentBlue
import com.eimemes.chat.ui.theme.AccentPurple
import com.eimemes.chat.util.HapticUtil

@Composable
fun AboutScreen(onBack: () -> Unit, onLicenses: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text("EimemesChat AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Version 2.9.0", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About this app", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        "EimemesChat AI is a powerful AI chat assistant built with Firebase and Groq. " +
                        "It supports web search, file attachments, conversation history, and personalization. " +
                        "Your conversations are securely stored and synced across all your devices.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            val context = LocalContext.current
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { HapticUtil.light(context); onLicenses() },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Description, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(14.dp))
                    Text("Open Source Licenses", fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.ChevronRight, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val licenses = listOf(
        Triple("Kotlin / Jetpack Compose", "Apache 2.0", "Copyright 2024 JetBrains / Google. Licensed under the Apache License, Version 2.0."),
        Triple("Firebase Android SDK", "Apache 2.0", "Copyright 2024 Google LLC. Licensed under the Apache License, Version 2.0."),
        Triple("OkHttp", "Apache 2.0", "Copyright 2024 Square, Inc. Licensed under the Apache License, Version 2.0."),
        Triple("Hilt (Dagger)", "Apache 2.0", "Copyright 2024 Google LLC. Licensed under the Apache License, Version 2.0."),
        Triple("Kotlinx Serialization", "Apache 2.0", "Copyright 2024 JetBrains. Licensed under the Apache License, Version 2.0."),
        Triple("Coil", "Apache 2.0", "Copyright 2024 Coil Contributors. Licensed under the Apache License, Version 2.0."),
        Triple("Retrofit", "Apache 2.0", "Copyright 2024 Square, Inc. Licensed under the Apache License, Version 2.0."),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text("Open Source Licenses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            licenses.forEach { (name, license, text) ->
                var expanded by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text(license, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(
                                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Text(
                                text,
                                fontSize = 11.sp,
                                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
