package com.eimemes.chat.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    private val KEY_DARK_MODE_SET = booleanPreferencesKey("dark_mode_set")

    val darkModeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        if (prefs[KEY_DARK_MODE_SET] == true) prefs[KEY_DARK_MODE] else null
    }

    suspend fun setDarkMode(dark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = dark
            prefs[KEY_DARK_MODE_SET] = true
        }
    }
}
