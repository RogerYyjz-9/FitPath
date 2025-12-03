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
    val stepsEnabled: Boolean = false,
    val stepsBaselineTotal: Long = 0L,
    val stepsBaselineEpochDay: Long = -1L,
    val dailyStepGoal: Int = 8000,
)

interface UserPrefsRepository {
    val prefs: StateFlow<UserPrefs>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguageMode(mode: LanguageMode)
    suspend fun setReminderEnabled(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setStepsEnabled(enabled: Boolean)
    suspend fun setStepsBaseline(total: Long, epochDay: Long)
    suspend fun setDailyStepGoal(goal: Int)
    suspend fun clearAll()
}
