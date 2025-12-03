// File: app/src/main/java/com/example/fitpath/data/prefs/UserDataStore.kt
package com.example.fitpath.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.fitpath.domain.model.ActivityLevel
import com.example.fitpath.domain.model.FoodPreference
import com.example.fitpath.domain.model.LanguageMode
import com.example.fitpath.domain.model.Sex
import com.example.fitpath.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fitpath_prefs")

class UserDataStore(private val context: Context) {
    private val DS = context.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val LANG = stringPreferencesKey("lang")
        val REMINDER = booleanPreferencesKey("reminder")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

        val STEPS_ENABLED = booleanPreferencesKey("steps_enabled")
        val STEPS_BASELINE_TOTAL = longPreferencesKey("steps_baseline_total")
        val STEPS_BASELINE_EPOCH_DAY = longPreferencesKey("steps_baseline_epoch_day")
        val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")

        val CURRENT_W = doublePreferencesKey("current_w")
        val TARGET_W = doublePreferencesKey("target_w")
        val ACTIVITY = stringPreferencesKey("activity")
        val PREF = stringPreferencesKey("food_pref")
        val SEX = stringPreferencesKey("sex")
        val AGE = intPreferencesKey("age")
        val AGE_SET = booleanPreferencesKey("age_set")
    }

    fun prefsFlow(): Flow<PrefsSnapshot> = DS.data.map { p ->
        PrefsSnapshot(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            languageMode = p[Keys.LANG]?.let { runCatching { LanguageMode.valueOf(it) }.getOrNull() } ?: LanguageMode.SYSTEM,
            reminderEnabled = p[Keys.REMINDER] ?: false,
            onboardingCompleted = p[Keys.ONBOARDING_DONE] ?: false,
            stepsEnabled = p[Keys.STEPS_ENABLED] ?: false,
            stepsBaselineTotal = p[Keys.STEPS_BASELINE_TOTAL] ?: 0L,
            stepsBaselineEpochDay = p[Keys.STEPS_BASELINE_EPOCH_DAY] ?: -1L,
            dailyStepGoal = p[Keys.DAILY_STEP_GOAL] ?: 8000,

            currentWeightKg = p[Keys.CURRENT_W],
            targetWeightKg = p[Keys.TARGET_W],
            activityLevel = p[Keys.ACTIVITY]?.let { runCatching { ActivityLevel.valueOf(it) }.getOrNull() } ?: ActivityLevel.MODERATE,
            foodPreference = p[Keys.PREF]?.let { runCatching { FoodPreference.valueOf(it) }.getOrNull() } ?: FoodPreference.NONE,
            sex = p[Keys.SEX]?.let { runCatching { Sex.valueOf(it) }.getOrNull() } ?: Sex.UNSPECIFIED,
            ageYears = if (p[Keys.AGE_SET] == true) p[Keys.AGE] else null
        )
    }

    // ✅ 关键：用 block body，返回 Unit
    suspend fun setTheme(mode: ThemeMode) {
        DS.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setLanguage(mode: LanguageMode) {
        DS.edit { it[Keys.LANG] = mode.name }
    }

    suspend fun setReminder(enabled: Boolean) {
        DS.edit { it[Keys.REMINDER] = enabled }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        DS.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setStepsEnabled(enabled: Boolean) {
        DS.edit { it[Keys.STEPS_ENABLED] = enabled }
    }

    suspend fun setStepsBaseline(total: Long, epochDay: Long) {
        DS.edit {
            it[Keys.STEPS_BASELINE_TOTAL] = total
            it[Keys.STEPS_BASELINE_EPOCH_DAY] = epochDay
        }
    }

    suspend fun setDailyStepGoal(goal: Int) {
        DS.edit { it[Keys.DAILY_STEP_GOAL] = goal }
    }

    suspend fun setProfile(
        currentWeightKg: Double?,
        targetWeightKg: Double?,
        activityLevel: ActivityLevel,
        foodPreference: FoodPreference,
        sex: Sex,
        ageYears: Int?
    ) {
        DS.edit { p ->
            if (currentWeightKg != null) p[Keys.CURRENT_W] = currentWeightKg else p.remove(Keys.CURRENT_W)
            if (targetWeightKg != null) p[Keys.TARGET_W] = targetWeightKg else p.remove(Keys.TARGET_W)
            p[Keys.ACTIVITY] = activityLevel.name
            p[Keys.PREF] = foodPreference.name
            p[Keys.SEX] = sex.name
            if (ageYears != null) {
                p[Keys.AGE] = ageYears
                p[Keys.AGE_SET] = true
            } else {
                p.remove(Keys.AGE)
                p[Keys.AGE_SET] = false
            }
        }
    }

    suspend fun clearAll() {
        DS.edit { it.clear() }
    }
}

data class PrefsSnapshot(
    val themeMode: ThemeMode,
    val languageMode: LanguageMode,
    val reminderEnabled: Boolean,
    val onboardingCompleted: Boolean,
    val stepsEnabled: Boolean,
    val stepsBaselineTotal: Long,
    val stepsBaselineEpochDay: Long,
    val dailyStepGoal: Int,

    val currentWeightKg: Double?,
    val targetWeightKg: Double?,
    val activityLevel: ActivityLevel,
    val foodPreference: FoodPreference,
    val sex: Sex,
    val ageYears: Int?
)
