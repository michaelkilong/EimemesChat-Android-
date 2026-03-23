package com.eimemes.chat.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eimemes.chat.data.local.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePrefs: ThemePreferences
) : ViewModel() {

    // null = follow system, true = dark, false = light
    val darkMode: StateFlow<Boolean?> = themePrefs.darkModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setDarkMode(dark: Boolean?) {
        viewModelScope.launch {
            if (dark == null) {
                // Reset to system — we store null as "not set"
                themePrefs.setDarkMode(false) // will be overridden by system
            } else {
                themePrefs.setDarkMode(dark)
            }
        }
    }
}
