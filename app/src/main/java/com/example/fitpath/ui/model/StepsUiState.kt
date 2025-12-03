package com.example.fitpath.ui.model

import kotlin.math.max

data class StepsUiState(
    val sensorSteps: Int = 0, // 传感器读数
    val manualSteps: Int = 0, // 用户手动输入
    val dailyGoal: Int = 10000,
    val hasPermission: Boolean = false
) {
    // 核心逻辑：UI 显示的步数是 传感器 和 手动输入 中的最大值
    val currentSteps: Int get() = max(sensorSteps, manualSteps)
}