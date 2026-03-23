package com.eimemes.chat.ui.personalization

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.eimemes.chat.util.HapticUtil

@Composable
fun PersonalizationScreen(
    onBack: () -> Unit,
    viewModel: PersonalizationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

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
            Text("Personalization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = {
                HapticUtil.success(context)
                viewModel.save(onBack)
            }, enabled = !state.saving) {
                if (state.saving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tone selector
            Text("Response tone", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            val tones = listOf("Friendly", "Professional", "Casual", "Concise")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tones.forEach { tone ->
                    FilterChip(
                        selected = state.tone == tone,
                        onClick  = { HapticUtil.light(context); viewModel.setTone(tone) },
                        label    = { Text(tone, fontSize = 13.sp) }
                    )
                }
            }

            PersonalField("Nickname", state.nickname, "What should I call you?") { viewModel.setNickname(it) }
            PersonalField("Occupation", state.occupation, "What do you do?") { viewModel.setOccupation(it) }
            PersonalField(
                "Custom instructions",
                state.customInstructions,
                "Any additional context or instructions…",
                minLines = 4
            ) { viewModel.setCustomInstructions(it) }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PersonalField(
    label: String,
    value: String,
    placeholder: String,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
            shape         = RoundedCornerShape(12.dp),
            minLines      = minLines,
            maxLines      = if (minLines > 1) 8 else 1,
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = com.eimemes.chat.ui.theme.AccentBlue
            )
        )
    }
}
