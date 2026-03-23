package com.eimemes.chat.ui.sidebar

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.eimemes.chat.domain.model.Conversation
import com.eimemes.chat.ui.theme.AccentBlue
import com.eimemes.chat.ui.theme.AccentPurple
import com.eimemes.chat.util.HapticUtil
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SidebarDrawer(
    open: Boolean,
    conversations: List<Conversation>,
    activeConvId: String?,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNewChat: () -> Unit,
    onClose: () -> Unit
) {
    AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClose() }
        )
    }

    AnimatedVisibility(visible = open, enter = slideInHorizontally { -it }, exit = slideOutHorizontally { -it }) {
        var searchQuery by remember { mutableStateOf("") }
        val filtered = if (searchQuery.isBlank()) conversations
        else conversations.filter { it.title.contains(searchQuery, ignoreCase = true) }

        Box(
            modifier = Modifier.fillMaxHeight().width(300.dp)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("EimemesChat AI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, "Close", Modifier.size(20.dp)) }
                }

                // ── Search bar ─────────────────────────────────────
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    placeholder = { Text("Search conversations…", fontSize = 13.sp) },
                    leadingIcon  = { Icon(Icons.Outlined.Search, null, Modifier.size(18.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Outlined.Clear, "Clear", Modifier.size(16.dp)) } }
                    } else null,
                    singleLine   = true,
                    shape        = RoundedCornerShape(12.dp),
                    colors       = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline)

                // ── New conversation ───────────────────────────────
                val context = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onNewChat() }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Outlined.Add, "New chat", Modifier.size(20.dp), tint = AccentBlue)
                    Text("New conversation", fontSize = 14.sp, color = AccentBlue, fontWeight = FontWeight.Medium)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // ── Conversation list ──────────────────────────────
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filtered, key = { it.id }) { conv ->
                        ConversationRow(
                            conv     = conv,
                            isActive = conv.id == activeConvId,
                            onSelect = { onSelect(conv.id) },
                            onDelete = { onDelete(conv.id) }
                        )
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                                Text(
                                    if (searchQuery.isBlank()) "No conversations yet" else "No results",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // ── Footer: conversation count ─────────────────────
                if (conversations.isNotEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding(), Alignment.Center) {
                        Text("${conversations.size} conversation${if (conversations.size != 1) "s" else ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(conv: Conversation, isActive: Boolean, onSelect: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap       = { onSelect() },
                    onLongPress = { HapticUtil.medium(context); showDialog = true }
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isActive) Icons.Outlined.ChatBubble else Icons.Outlined.ChatBubbleOutline,
            null, Modifier.size(16.dp),
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                conv.title,
                fontSize   = 13.sp,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                color      = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
            )
            if (conv.updatedAt > 0) {
                Text(formatDate(conv.updatedAt), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title   = { Text("Delete conversation?") },
            text    = { Text("\"${conv.title}\" will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false; onDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun formatDate(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    return when {
        diff < 60_000L      -> "Just now"
        diff < 3_600_000L   -> "${diff / 60_000}m ago"
        diff < 86_400_000L  -> "${diff / 3_600_000}h ago"
        diff < 604_800_000L -> "${diff / 86_400_000}d ago"
        else                -> SimpleDateFormat("MMM d", Locale.US).format(Date(ts))
    }
}
