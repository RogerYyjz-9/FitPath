// File: app/src/main/java/com/fitpath/domain/prefs/UserPrefsRepository.kt
package com.example.fitpath.domain.prefs

import com.example.fitpath.domain.model.LanguageMode
import com.example.fitpath.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

data class UserPrefs(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageMode: LanguageMode = LanguageMode.SYSTEM,
    val reminderEnabled: Boolean = false,
    val onboardingCompleted: Boolean = false,
)

interface UserPrefsRepository {
    val prefs: StateFlow<UserPrefs>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguageMode(mode: LanguageMode)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun clearAll()
}
