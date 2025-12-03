// File: app/src/main/java/com/example/fitpath/ui/vm/AppViewModel.kt
package com.example.fitpath.ui.vm

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitpath.core.AppContainer
import com.example.fitpath.domain.model.ActivityLevel
import com.example.fitpath.domain.model.FoodPreference
import com.example.fitpath.domain.model.LanguageMode
import com.example.fitpath.domain.model.Sex
import com.example.fitpath.domain.model.ThemeMode
import com.example.fitpath.domain.plan.PlanEngine
import com.example.fitpath.domain.plan.TodayPlan
import com.example.fitpath.domain.profile.UserProfile
import com.example.fitpath.domain.weight.WeightEntryModel
import com.example.fitpath.ui.model.StepsUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiState(
    val plan: TodayPlan? = null,
    val planError: Boolean = false,
    val workoutDone: Boolean = false,
    val weightEntries: List<WeightEntryModel> = emptyList(),
    val exporting: Boolean = false,
    val exportText: String? = null,
    val stepsUi: StepsUiState = StepsUiState()
)

class AppViewModel(
    private val container: AppContainer
) : ViewModel() {

    val prefs = container.userPrefsRepository.prefs
    val profile = container.userProfileRepository.profile

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    init {
        container.weightRepository.entriesFlow()
            .onEach { entries -> _ui.update { it.copy(weightEntries = entries) } }
            .launchIn(viewModelScope)

        profile
            .onEach { p -> refreshPlanFor(p) }
            .launchIn(viewModelScope)

        prefs
            .map { it.reminderEnabled }
            .distinctUntilChanged()
            .onEach { enabled ->
                if (enabled) container.reminderScheduler.scheduleDaily()
                else container.reminderScheduler.cancel()
            }
            .launchIn(viewModelScope)

        // 监听传感器步数
        container.stepCounterManager.steps
            .onEach { steps ->
                _ui.update { it.copy(stepsUi = it.stepsUi.copy(sensorSteps = steps)) }
            }
            .launchIn(viewModelScope)
    }

    // 设置手动步数
    fun setManualSteps(steps: Int) {
        _ui.update { it.copy(stepsUi = it.stepsUi.copy(manualSteps = steps)) }
    }

    fun enableSteps(granted: Boolean) {
        if (granted) {
            container.stepCounterManager.startListening()
        }
        setStepsPermission(granted)
    }

    fun setStepsPermission(granted: Boolean) {
        _ui.update { it.copy(stepsUi = it.stepsUi.copy(hasPermission = granted)) }
    }

    fun isOnboardingCompleted(): Boolean = prefs.value.onboardingCompleted

    fun completeOnboarding() {
        viewModelScope.launch { container.userPrefsRepository.setOnboardingCompleted(true) }
    }

    fun updateProfile(
        currentWeightKg: Double?,
        targetWeightKg: Double?,
        activityLevel: ActivityLevel,
        foodPreference: FoodPreference,
        sex: Sex,
        ageYears: Int?,
        heightCm: Int? // [新增]
    ) {
        viewModelScope.launch {
            container.userProfileRepository.update(
                UserProfile(
                    currentWeightKg = currentWeightKg,
                    targetWeightKg = targetWeightKg,
                    activityLevel = activityLevel,
                    foodPreference = foodPreference,
                    sex = sex,
                    ageYears = ageYears,
                    heightCm = heightCm // [新增]
                )
            )
        }
    }

    private fun refreshPlanFor(p: UserProfile) {
        if (!p.isReadyForPlan()) {
            _ui.update { it.copy(plan = null, planError = false) }
            return
        }
        val res = PlanEngine.generate(p)
        _ui.update {
            if (res.isSuccess) it.copy(plan = res.getOrNull(), planError = false)
            else it.copy(plan = null, planError = true)
        }
    }

    fun toggleWorkoutDone() = _ui.update { it.copy(workoutDone = !it.workoutDone) }

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { container.userPrefsRepository.setThemeMode(mode) }
    fun setLanguage(mode: LanguageMode) = viewModelScope.launch { container.userPrefsRepository.setLanguageMode(mode) }
    fun setReminderEnabled(enabled: Boolean) = viewModelScope.launch { container.userPrefsRepository.setReminderEnabled(enabled) }

    fun upsertWeight(date: LocalDate, weightKg: Double) = viewModelScope.launch { container.weightRepository.upsert(date, weightKg) }
    fun deleteWeight(id: Long) = viewModelScope.launch { container.weightRepository.delete(id) }

    fun exportData(context: Context) {
        viewModelScope.launch {
            _ui.update { it.copy(exporting = true, exportText = null) }
            val snapPrefs = prefs.value
            val snapProfile = profile.value
            val entries = ui.value.weightEntries.sortedBy { it.date }
            val json = buildJsonExport(snapPrefs, snapProfile, entries)
            _ui.update { it.copy(exporting = false, exportText = json) }
            shareText(context, json)
        }
    }

    private fun shareText(context: Context, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "FitPath export")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, "Export FitPath data").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ContextCompat.startActivity(context, chooser, null)
    }

    private fun buildJsonExport(
        prefs: com.example.fitpath.domain.prefs.UserPrefs,
        profile: UserProfile,
        entries: List<WeightEntryModel>
    ): String {
        val entriesJson = entries.joinToString(separator = ",") { e ->
            "{\"date\":\"${e.date}\",\"weightKg\":${"%.2f".format(e.weightKg)}}"
        }
        return "{\n" +
                "  \"prefs\": { \"theme\": \"${prefs.themeMode}\", \"lang\": \"${prefs.languageMode}\", \"reminder\": ${prefs.reminderEnabled} },\n" +
                "  \"profile\": { \"currentWeightKg\": ${profile.currentWeightKg ?: "null"}, \"targetWeightKg\": ${profile.targetWeightKg ?: "null"}, \"activityLevel\": \"${profile.activityLevel}\", \"foodPreference\": \"${profile.foodPreference}\", \"sex\": \"${profile.sex}\", \"ageYears\": ${profile.ageYears ?: "null"} },\n" +
                "  \"weightEntries\": [ $entriesJson ]\n" +
                "}\n"
    }

    fun deleteAllData() {
        viewModelScope.launch {
            container.weightRepository.clearAll()
            container.userPrefsRepository.clearAll()
            _ui.value = UiState()
        }
    }
}