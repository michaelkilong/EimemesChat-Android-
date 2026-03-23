package com.eimemes.chat.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eimemes.chat.R
import com.eimemes.chat.ui.theme.AccentBlue
import com.eimemes.chat.ui.theme.AccentPurple

@OptIn(ExperimentalTextApi::class)
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var agreedToTerms by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleGoogleSignInResult(result.data)
        }
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(AccentBlue, AccentPurple),
        start = Offset(0f, 0f),
        end   = Offset(400f, 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1A1A2E))
                .border(1.dp, Color(0xFF2A2A3A), RoundedCornerShape(20.dp))
                .padding(28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Gradient title
                Text(
                    "EimemesChat AI",
                    style = TextStyle(
                        brush      = gradientBrush,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    "Create your account to get started",
                    fontSize  = 13.sp,
                    color     = Color(0xFF8888AA),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Email field
                OutlinedTextField(
                    value         = "",
                    onValueChange = {},
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Email", color = Color(0xFF666688)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF2A2A3A),
                        focusedBorderColor   = AccentBlue,
                        unfocusedContainerColor = Color(0xFF12121F),
                        focusedContainerColor   = Color(0xFF12121F),
                        unfocusedTextColor   = Color.White,
                        focusedTextColor     = Color.White
                    )
                )

                // Password field
                OutlinedTextField(
                    value         = "",
                    onValueChange = {},
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Password", color = Color(0xFF666688)) },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon  = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                null,
                                tint = Color(0xFF666688)
                            )
                        }
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF2A2A3A),
                        focusedBorderColor   = AccentBlue,
                        unfocusedContainerColor = Color(0xFF12121F),
                        focusedContainerColor   = Color(0xFF12121F),
                        unfocusedTextColor   = Color.White,
                        focusedTextColor     = Color.White
                    )
                )

                // Create Account button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(AccentBlue, AccentPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Create Account", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2A2A3A))
                    Text("or", fontSize = 12.sp, color = Color(0xFF666688))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2A2A3A))
                }

                // Google Sign-In button
                OutlinedButton(
                    onClick = {
                        if (agreedToTerms) {
                            val webClientId = context.getString(R.string.default_web_client_id)
                            launcher.launch(viewModel.getGoogleSignInIntent(webClientId))
                        }
                    },
                    enabled  = !state.loading && agreedToTerms,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF12121F)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A3A))
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AccentBlue)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Google G icon
                            Text("G", color = AccentBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Continue with Google", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                // Terms checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked  = agreedToTerms,
                        onCheckedChange = { agreedToTerms = it },
                        colors   = CheckboxDefaults.colors(
                            checkedColor   = AccentBlue,
                            uncheckedColor = Color(0xFF666688)
                        )
                    )
                    Text(
                        "I agree to the ",
                        fontSize = 12.sp,
                        color    = Color(0xFF8888AA)
                    )
                    Text(
                        "Terms",
                        fontSize = 12.sp,
                        color    = AccentBlue,
                        textDecoration = TextDecoration.Underline
                    )
                    Text(" and ", fontSize = 12.sp, color = Color(0xFF8888AA))
                    Text(
                        "Privacy Policy",
                        fontSize = 12.sp,
                        color    = AccentBlue,
                        textDecoration = TextDecoration.Underline
                    )
                }

                // Error
                state.error?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center)
                }

                // Sign in link
                TextButton(onClick = {}) {
                    Text(
                        "Already have an account? ",
                        fontSize = 12.sp,
                        color    = Color(0xFF8888AA)
                    )
                    Text(
                        "Sign in",
                        fontSize = 12.sp,
                        color    = AccentBlue,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }
}
