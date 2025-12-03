// File: app/src/main/java/com/example/fitpath/ui/screens/TodayPlanScreen.kt
package com.example.fitpath.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fitpath.R
import com.example.fitpath.domain.plan.MealSuggestion
import com.example.fitpath.domain.plan.TodayPlan
import com.example.fitpath.ui.model.StepsUiState
import com.example.fitpath.ui.vm.AppViewModel

@Composable
fun TodayPlanScreen(
    vm: AppViewModel,
    onEditProfile: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    val stepsState = ui.stepsUi

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 控制修改步数的弹窗
    var showStepDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        vm.enableSteps(isGranted)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) vm.enableSteps(true)
        } else {
            vm.enableSteps(true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.today_title), style = MaterialTheme.typography.headlineSmall)

        // 步数卡片 (带圆环)
        StepsCard(
            stepsState = stepsState,
            onRequestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            },
            onEditSteps = { showStepDialog = true }
        )

        // 错误处理
        if (ui.planError) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.error_invalid_input))
                    OutlinedButton(onClick = onEditProfile) { Text(stringResource(R.string.edit_profile)) }
                }
            }
            return
        }

        val plan = ui.plan
        if (plan == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Complete onboarding to generate your plan.")
                    OutlinedButton(onClick = onEditProfile) { Text(stringResource(R.string.edit_profile)) }
                }
            }
            return
        }

        // 计划内容
        PlanSummary(plan)
        MealBlock(title = stringResource(R.string.breakfast), meal = plan.breakfast)
        MealBlock(title = stringResource(R.string.lunch), meal = plan.lunch)
        MealBlock(title = stringResource(R.string.dinner), meal = plan.dinner)
        WorkoutBlock(plan = plan, done = ui.workoutDone, onToggle = { vm.toggleWorkoutDone() })

        Text(text = stringResource(R.string.safety_note), style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(32.dp))
    }

    // 弹窗逻辑
    if (showStepDialog) {
        StepsEntryDialog(
            currentManual = stepsState.manualSteps,
            onDismiss = { showStepDialog = false },
            onConfirm = { newSteps ->
                vm.setManualSteps(newSteps)
                showStepDialog = false
            }
        )
    }
}

// 步数卡片组件：加入圆环进度条
@Composable
private fun StepsCard(
    stepsState: StepsUiState,
    onRequestPermission: () -> Unit,
    onEditSteps: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Steps Today", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    if (stepsState.hasPermission) {
                        IconButton(
                            onClick = onEditSteps,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit steps", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (stepsState.hasPermission) {
                    val progress = (stepsState.currentSteps.toFloat() / stepsState.dailyGoal.toFloat()).coerceIn(0f, 1f)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // [新增] 圆环进度条
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f }, // 底色圆环 (全灰)
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            CircularProgressIndicator(
                                progress = { progress }, // 进度圆环
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            // 中间显示百分比
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(Modifier.width(16.dp))

                        // 步数文字
                        Column {
                            Text(
                                text = "${stepsState.currentSteps}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "/ ${stepsState.dailyGoal}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (stepsState.manualSteps > stepsState.sensorSteps && stepsState.manualSteps > 0) {
                                Text(
                                    text = "(Manual)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    Text("Permission needed", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!stepsState.hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Button(onClick = onRequestPermission) {
                    Text("Enable")
                }
            }
        }
    }
}

// 计划摘要：修改文字样式
@Composable
private fun PlanSummary(plan: TodayPlan) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // [修改] 缩小说明文字
            Text(
                text = plan.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            HorizontalDivider()

            // [修改] 突出卡路里数字
            Column {
                Text(
                    text = stringResource(R.string.calorie_range),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = "${plan.calories.min} – ${plan.calories.max}",
                    style = MaterialTheme.typography.displaySmall, // 变大
                    fontWeight = FontWeight.Bold, // 加粗
                    color = MaterialTheme.colorScheme.primary
                )
                Text("kcal", style = MaterialTheme.typography.bodyMedium)
            }

            // 宏量营养素
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${stringResource(R.string.macro_ranges)}:", style = MaterialTheme.typography.titleSmall)
                MacroRow(stringResource(R.string.carbs), "${plan.macros.carbsG.min}–${plan.macros.carbsG.max} g")
                MacroRow(stringResource(R.string.protein), "${plan.macros.proteinG.min}–${plan.macros.proteinG.max} g")
                MacroRow(stringResource(R.string.fat), "${plan.macros.fatG.min}–${plan.macros.fatG.max} g")
            }
        }
    }
}

@Composable
private fun MacroRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("• $label", style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MealBlock(title: String, meal: MealSuggestion) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text("${meal.approxCalories} kcal", style = MaterialTheme.typography.labelMedium)
            }
            Text(meal.title, style = MaterialTheme.typography.titleSmall)
            Text(meal.description, style = MaterialTheme.typography.bodyMedium)

            if (meal.substitutions.isNotEmpty()) {
                Text(stringResource(R.string.swap), style = MaterialTheme.typography.labelLarge)
                // 横向滚动
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    meal.substitutions.forEach { sub ->
                        AssistChip(onClick = { }, label = { Text(sub.title) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutBlock(plan: TodayPlan, done: Boolean, onToggle: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.workout), style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = onToggle) {
                    Text(if (done) stringResource(R.string.workout_undone) else stringResource(R.string.workout_done))
                }
            }
            Text(plan.workout.title, style = MaterialTheme.typography.titleSmall)
            Text(plan.workout.details)

            if (plan.workout.substitutions.isNotEmpty()) {
                Text(stringResource(R.string.swap), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    plan.workout.substitutions.forEach {
                        AssistChip(onClick = { }, label = { Text(it.title) })
                    }
                }
            }
        }
    }
}

// 手动输入步数的弹窗
@Composable
private fun StepsEntryDialog(
    currentManual: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(if (currentManual > 0) currentManual.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual Steps") },
        text = {
            Column {
                Text("Enter steps from other devices. We use the higher value.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { char -> char.isDigit() } },
                    label = { Text("Steps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val steps = text.toIntOrNull() ?: 0
                    onConfirm(steps)
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)