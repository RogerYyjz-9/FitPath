// File: app/src/main/java/com/fitpath/data/repo/UserPrefsRepositoryImpl.kt
package com.example.fitpath.data.repo

import com.example.fitpath.data.prefs.UserDataStore
import com.example.fitpath.domain.model.LanguageMode
import com.example.fitpath.domain.model.ThemeMode
import com.example.fitpath.domain.prefs.UserPrefs
import com.example.fitpath.domain.prefs.UserPrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UserPrefsRepositoryImpl(
    private val store: UserDataStore
) : UserPrefsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _prefs = MutableStateFlow(UserPrefs())
    override val prefs: StateFlow<UserPrefs> = _prefs

    init {
        store.prefsFlow()
            .onEach { snap ->
                _prefs.value = UserPrefs(
                    themeMode = snap.themeMode,
                    languageMode = snap.languageMode,
                    reminderEnabled = snap.reminderEnabled,
                    onboardingCompleted = snap.onboardingCompleted,
                    stepsEnabled = snap.stepsEnabled,
                    stepsBaselineTotal = snap.stepsBaselineTotal,
                    stepsBaselineEpochDay = snap.stepsBaselineEpochDay,
                    dailyStepGoal = snap.dailyStepGoal
                )
            }
            .launchIn(scope)
    }

    override suspend fun setThemeMode(mode: ThemeMode) = store.setTheme(mode)
    override suspend fun setLanguageMode(mode: LanguageMode) = store.setLanguage(mode)
    override suspend fun setReminderEnabled(enabled: Boolean) = store.setReminder(enabled)
    override suspend fun setOnboardingCompleted(completed: Boolean) = store.setOnboardingCompleted(completed)
    override suspend fun setStepsEnabled(enabled: Boolean) = store.setStepsEnabled(enabled)
    override suspend fun setStepsBaseline(total: Long, epochDay: Long) = store.setStepsBaseline(total, epochDay)
    override suspend fun setDailyStepGoal(goal: Int) = store.setDailyStepGoal(goal)
    override suspend fun clearAll() = store.clearAll()
}
