package com.eimemes.chat.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

object BrowserUtil {
    /**
     * Opens a URL in Chrome Custom Tabs (in-app browser).
     * Falls back to the system browser if Custom Tabs aren't available.
     */
    fun openUrl(context: Context, url: String, darkTheme: Boolean = true) {
        if (url.isBlank()) return
        runCatching {
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(if (darkTheme) 0xFF141417.toInt() else 0xFFE9EAF2.toInt())
                .build()

            CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(colorSchemeParams)
                .setColorScheme(
                    if (darkTheme) CustomTabsIntent.COLOR_SCHEME_DARK
                    else CustomTabsIntent.COLOR_SCHEME_LIGHT
                )
                .setShowTitle(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .build()
                .launchUrl(context, Uri.parse(url))
        }
    }
}
