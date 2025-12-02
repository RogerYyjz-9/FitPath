// File: app/src/main/java/com/fitpath/ui/theme/Theme.kt
package com.example.fitpath.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.fitpath.domain.model.ThemeMode

@Composable
fun FitPathTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dark ->
            dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !dark ->
            dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colors, content = content)
}
