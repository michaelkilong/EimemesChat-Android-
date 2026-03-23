package com.eimemes.chat.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eimemes.chat.ui.about.AboutScreen
import com.eimemes.chat.ui.about.LicensesScreen
import com.eimemes.chat.ui.auth.AuthViewModel
import com.eimemes.chat.ui.auth.LoginScreen
import com.eimemes.chat.ui.chat.ChatScreen
import com.eimemes.chat.ui.chat.ChatViewModel
import com.eimemes.chat.ui.personalization.PersonalizationScreen
import com.eimemes.chat.ui.settings.SettingsScreen
import kotlinx.coroutines.delay

object Routes {
    const val CHAT            = "chat"
    const val SETTINGS        = "settings"
    const val PERSONALIZATION = "personalization"
    const val ABOUT           = "about"
    const val LICENSES        = "licenses"
}

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.state.collectAsState()

    // Show splash for minimum 2 seconds while auth state loads
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    when {
        showSplash || authState.loading -> {
            SplashScreen()
        }
        authState.user == null -> {
            LoginScreen(viewModel = authViewModel)
        }
        else -> {
            val navController = rememberNavController()
            val chatViewModel: ChatViewModel = hiltViewModel()

            NavHost(navController = navController, startDestination = Routes.CHAT) {
                composable(Routes.CHAT) {
                    ChatScreen(
                        viewModel          = chatViewModel,
                        onNavigateSettings = { navController.navigate(Routes.SETTINGS) }
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        authViewModel             = authViewModel,
                        onBack                    = { navController.popBackStack() },
                        onNavigatePersonalization = { navController.navigate(Routes.PERSONALIZATION) },
                        onNavigateAbout           = { navController.navigate(Routes.ABOUT) }
                    )
                }
                composable(Routes.PERSONALIZATION) {
                    PersonalizationScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ABOUT) {
                    AboutScreen(
                        onBack     = { navController.popBackStack() },
                        onLicenses = { navController.navigate(Routes.LICENSES) }
                    )
                }
                composable(Routes.LICENSES) {
                    LicensesScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
