package com.eimemes.chat.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.eimemes.chat.ui.auth.AuthViewModel
import com.eimemes.chat.util.HapticUtil

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigatePersonalization: () -> Unit,
    onNavigateAbout: () -> Unit
) {
    val authState by authViewModel.state.collectAsState()
    val context = LocalContext.current
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back") }
            Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            authState.user?.let { user ->
                SettingsCard {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (user.displayName?.firstOrNull() ?: user.email?.firstOrNull() ?: 'U').uppercaseChar().toString(),
                                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column {
                            user.displayName?.let { Text(it, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
                            user.email?.let { Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionLabel("Preferences")
            SettingsCard {
                SettingsRow(Icons.Outlined.Tune, "Personalization") {
                    HapticUtil.light(context); onNavigatePersonalization()
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionLabel("About")
            SettingsCard {
                SettingsRow(Icons.Outlined.Info, "About EimemesChat AI") {
                    HapticUtil.light(context); onNavigateAbout()
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionLabel("Account")
            SettingsCard {
                SettingsRow(Icons.Outlined.Logout, "Sign out", tint = MaterialTheme.colorScheme.error) {
                    HapticUtil.medium(context); showSignOutDialog = true
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "EimemesChat AI v2.9.0",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title   = { Text("Sign out?") },
            text    = { Text("You'll need to sign in again to use EimemesChat AI.") },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; authViewModel.signOut() }) {
                    Text("Sign out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) { Column { content() } }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = tint)
        Text(label, fontSize = 14.sp, color = tint, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
