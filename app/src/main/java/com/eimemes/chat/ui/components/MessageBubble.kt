package com.eimemes.chat.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eimemes.chat.domain.model.Message
import com.eimemes.chat.domain.model.Source
import com.eimemes.chat.ui.theme.AccentBlue
import com.eimemes.chat.ui.theme.AccentPurple
import com.eimemes.chat.util.BrowserUtil
import com.eimemes.chat.util.HapticUtil

@Composable
fun MessageBubble(message: Message, modifier: Modifier = Modifier) {
    val isUser = message.role == "user"
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) UserBubble(message.content) else AssistantBubble(message)
        if (message.time.isNotBlank()) {
            Text(
                message.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun UserBubble(content: String) {
    Box(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
            .background(Brush.linearGradient(listOf(AccentBlue, AccentPurple)))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(content, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
    }
}

@Composable
fun AssistantBubble(message: Message) {
    val context   = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var sourcesExpanded by remember { mutableStateOf(false) }
    var expandedSourceIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxWidth(0.95f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MarkdownText(
                text = message.content,
                baseColor = MaterialTheme.colorScheme.onSurface,
                onCitationClick = { index ->
                    HapticUtil.light(context)
                    sourcesExpanded = true
                    expandedSourceIndex = index - 1
                }
            )
        }

        message.sources?.takeIf { it.isNotEmpty() }?.let { sources ->
            Spacer(Modifier.height(6.dp))
            SourcesPill(
                sources = sources,
                expanded = sourcesExpanded,
                expandedIndex = expandedSourceIndex,
                onToggle = {
                    HapticUtil.light(context)
                    sourcesExpanded = !sourcesExpanded
                    if (!sourcesExpanded) expandedSourceIndex = null
                }
            )
        }

        message.disclaimer?.let { DisclaimerRow(it) }

        Row(modifier = Modifier.padding(top = 2.dp)) {
            IconButton(onClick = {
                HapticUtil.light(context)
                clipboard.setText(AnnotatedString(message.content))
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.ContentCopy, "Copy", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun StreamingBubble(text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MarkdownText(text = text, baseColor = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SourcesPill(
    sources: List<Source>,
    expanded: Boolean,
    expandedIndex: Int?,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    Column {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.Language, null, Modifier.size(14.dp), tint = AccentBlue)
            Text("${sources.size} sources", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sources.forEachIndexed { i, src ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (expandedIndex == i) AccentBlue.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                HapticUtil.light(context)
                                BrowserUtil.openUrl(context, src.url)
                            }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("${i + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        Column(Modifier.weight(1f)) {
                            Text(src.title, fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                Uri.parse(src.url).host ?: src.url,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Outlined.OpenInNew, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisclaimerRow(disclaimer: String) {
    val text = when (disclaimer) {
        "critical" -> "For informational purposes only. Consult a qualified professional."
        "web"      -> "Web sources may be outdated. Verify from authoritative sources."
        else       -> disclaimer
    }
    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Outlined.Info, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
    }
}
