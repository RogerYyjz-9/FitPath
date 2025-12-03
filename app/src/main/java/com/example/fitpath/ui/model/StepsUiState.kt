package com.example.fitpath.ui.model

data class StepsUiState(
    val status: StepsStatus = StepsStatus.Disabled,
    val stepsToday: Int = 0,
    val goal: Int = 8000
)

sealed class StepsStatus {
    object Disabled : StepsStatus()
    object PermissionNeeded : StepsStatus()
    object Unsupported : StepsStatus()
    object Loading : StepsStatus()
    object Ready : StepsStatus()
}
