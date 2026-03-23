package com.eimemes.chat.ui.chat

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
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
import com.eimemes.chat.ui.components.*
import com.eimemes.chat.ui.sidebar.SidebarDrawer
import com.eimemes.chat.ui.theme.AccentBlue
import com.eimemes.chat.ui.theme.AccentPurple
import com.eimemes.chat.util.FilePickerUtil
import com.eimemes.chat.util.HapticUtil
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateSettings: () -> Unit
) {
    val state        = viewModel.state.collectAsState().value
    val context      = LocalContext.current
    val listState    = rememberLazyListState()
    val coroutine    = rememberCoroutineScope()

    // Auto-scroll to bottom on new content
    val msgCount = state.messages.size + (if (state.streamingText.isNotEmpty()) 1 else 0)
    LaunchedEffect(msgCount) {
        if (msgCount > 0) listState.animateScrollToItem(msgCount - 1)
    }

    // File picker launcher
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutine.launch {
                FilePickerUtil.processUri(context, uri)
                    .onSuccess  { viewModel.setAttachment(it) }
                    .onFailure  { viewModel.showError(it.message ?: "Could not read file") }
            }
        }
    }

    BackHandler(enabled = state.sidebarOpen) { viewModel.setSidebarOpen(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────
            TopBar(
                onMenuClick    = { HapticUtil.light(context); viewModel.setSidebarOpen(true) },
                onNewChat      = { viewModel.newConversation() },
                onSettingsClick = onNavigateSettings
            )

            // ── Messages ─────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (state.messages.isEmpty() && !state.isStreaming) {
                    WelcomeScreen(isFirstSession = state.isFirstSession) { text ->
                        viewModel.onInputChange(text)
                        viewModel.sendMessage()
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.messages, key = { it.time + it.role }) { msg ->
                            MessageBubble(message = msg)
                        }
                        when {
                            state.isSearching -> item { SearchingIndicator() }
                            state.isStreaming && state.streamingText.isEmpty() -> item {
                                Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                    TypingIndicator()
                                }
                            }
                            state.streamingText.isNotEmpty() -> item { StreamingBubble(state.streamingText) }
                        }
                    }

                    // Scroll-to-bottom button
                    val showScroll by remember { derivedStateOf { listState.canScrollForward } }
                    AnimatedVisibility(
                        visible  = showScroll,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        enter    = fadeIn() + scaleIn(),
                        exit     = fadeOut() + scaleOut()
                    ) {
                        SmallFloatingActionButton(
                            onClick = {
                                HapticUtil.light(context)
                                coroutine.launch { listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1) }
                            },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Icon(Icons.Outlined.KeyboardArrowDown, "Scroll to bottom", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ── Error banner ─────────────────────────────────────
            state.error?.let { err ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(err, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                        IconButton(onClick = { viewModel.clearError() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Close, "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Input bar ────────────────────────────────────────
            InputBar(
                text          = state.inputText,
                onTextChange  = viewModel::onInputChange,
                onSend        = { if (state.isStreaming) viewModel.stopStreaming() else viewModel.sendMessage() },
                isStreaming   = state.isStreaming,
                webSearchOn   = state.webSearchEnabled,
                onWebToggle   = viewModel::toggleWebSearch,
                attachmentName = state.attachment?.name,
                onAttachClick  = { fileLauncher.launch("*/*") },
                onAttachClear  = { viewModel.setAttachment(null) }
            )
        }

        // ── Sidebar ───────────────────────────────────────────────
        SidebarDrawer(
            open          = state.sidebarOpen,
            conversations = state.conversations,
            activeConvId  = state.activeConvId,
            onSelect      = viewModel::selectConversation,
            onDelete      = viewModel::deleteConversation,
            onNewChat     = viewModel::newConversation,
            onClose       = { viewModel.setSidebarOpen(false) }
        )
    }
}

// ── Top bar ────────────────────────────────────────────────────────
@Composable
private fun TopBar(onMenuClick: () -> Unit, onNewChat: () -> Unit, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarButton(onClick = onMenuClick) { Icon(Icons.Outlined.Menu, "Menu", Modifier.size(20.dp)) }
        Spacer(Modifier.width(10.dp))
        Text(
            "EimemesChat AI",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        TopBarButton(onClick = onNewChat)     { Icon(Icons.Outlined.Edit, "New chat", Modifier.size(20.dp)) }
        Spacer(Modifier.width(8.dp))
        TopBarButton(onClick = onSettingsClick) { Icon(Icons.Outlined.Settings, "Settings", Modifier.size(20.dp)) }
    }
}

@Composable
private fun TopBarButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { content() }
}

// ── Welcome screen ─────────────────────────────────────────────────
@Composable
private fun WelcomeScreen(isFirstSession: Boolean, onSuggestion: (String) -> Unit) {
    val context = LocalContext.current
    val suggestions = listOf(
        "What can you help me with?",
        "Explain quantum computing simply",
        "Write a short poem about the ocean",
        "Help me debug my code"
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("EimemesChat AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("How can I help you today?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (isFirstSession) {
            Spacer(Modifier.height(28.dp))
            suggestions.forEach { s ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .clickable { HapticUtil.light(context); onSuggestion(s) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) { Text(s, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface) }
            }
        }
    }
}

// ── Searching indicator ────────────────────────────────────────────
@Composable
fun SearchingIndicator() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = AccentBlue)
        Text("Searching the web…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Input bar ──────────────────────────────────────────────────────
@Composable
private fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isStreaming: Boolean,
    webSearchOn: Boolean,
    onWebToggle: () -> Unit,
    attachmentName: String?,
    onAttachClick: () -> Unit,
    onAttachClear: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color    = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {

            // Attachment chip
            if (attachmentName != null) {
                Row(
                    modifier = Modifier.padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.AttachFile, null, Modifier.size(13.dp), tint = AccentBlue)
                    Text(attachmentName, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                    Icon(
                        Icons.Outlined.Close, "Remove attachment",
                        modifier = Modifier.size(13.dp).clickable { onAttachClear() },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Attach button
                InputIconButton(onClick = onAttachClick) {
                    Icon(Icons.Outlined.AttachFile, "Attach file", Modifier.size(18.dp))
                }

                // Globe (web search)
                InputIconButton(onClick = onWebToggle, highlighted = webSearchOn) {
                    Icon(Icons.Outlined.Language, "Web search", Modifier.size(18.dp),
                        tint = if (webSearchOn) AccentBlue else MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Text field
                OutlinedTextField(
                    value         = text,
                    onValueChange = onTextChange,
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text("Message…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                    shape         = RoundedCornerShape(22.dp),
                    maxLines      = 6,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = AccentBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Send / Stop
                val canSend = isStreaming || text.isNotBlank()
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(if (canSend) Brush.linearGradient(listOf(AccentBlue, AccentPurple)) else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)))
                        .clickable(enabled = canSend) { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isStreaming) Icons.Outlined.Stop else Icons.Outlined.Send,
                        "Send",
                        Modifier.size(18.dp),
                        tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Active indicators
            if (webSearchOn || attachmentName != null) {
                Row(modifier = Modifier.padding(top = 4.dp, start = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (webSearchOn) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Language, null, Modifier.size(11.dp), tint = AccentBlue)
                            Text("Web search on", fontSize = 11.sp, color = AccentBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputIconButton(onClick: () -> Unit, highlighted: Boolean = false, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape)
            .background(if (highlighted) AccentBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, if (highlighted) AccentBlue else MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { content() }
}
