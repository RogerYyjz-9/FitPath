// File: app/src/main/java/com/fitpath/ui/screens/TodayPlanScreen.kt
package com.example.fitpath.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.fitpath.R
import com.example.fitpath.domain.plan.MealSuggestion
import com.example.fitpath.domain.plan.TodayPlan
import com.example.fitpath.ui.model.StepsStatus
import com.example.fitpath.ui.vm.AppViewModel
import kotlin.math.min

@Composable
fun TodayPlanScreen(
    vm: AppViewModel,
    onEditProfile: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    val stepsUi by vm.stepsUi.collectAsState()
    val plan = ui.plan
    val scrollNonce by vm.todayScrollToMealsNonce.collectAsState()
    val context = LocalContext.current
    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACTIVITY_RECOGNITION
    ) == PackageManager.PERMISSION_GRANTED
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        vm.setStepsPermission(granted)
        if (granted) vm.enableSteps()
    }

    LaunchedEffect(permissionGranted) {
        vm.setStepsPermission(permissionGranted)
    }

    if (ui.planError) {
        ErrorCard(onEditProfile = onEditProfile)
        return
    }

    if (plan == null) {
        NeedOnboardingCard(onEditProfile = onEditProfile)
        return
    }

    val listState = rememberLazyListState()
    val mealsIndex = 3
    LaunchedEffect(scrollNonce) {
        runCatching { listState.animateScrollToItem(mealsIndex) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item("title") {
            Text(text = stringResource(R.string.today_title), style = MaterialTheme.typography.headlineSmall)
        }
        item("steps") {
            StepsCard(
                stepsUi = stepsUi,
                onEnable = {
                    if (permissionGranted) vm.enableSteps() else permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            )
        }
        item("summary") { PlanSummary(plan) }
        item("breakfast") { MealBlock(title = stringResource(R.string.breakfast), meal = plan.breakfast) }
        item("lunch") { MealBlock(title = stringResource(R.string.lunch), meal = plan.lunch) }
        item("dinner") { MealBlock(title = stringResource(R.string.dinner), meal = plan.dinner) }
        item("workout") { WorkoutBlock(plan = plan, done = ui.workoutDone, onToggle = { vm.toggleWorkoutDone() }) }
        item("safety") { Text(text = stringResource(R.string.safety_note), style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun StepsCard(
    stepsUi: com.example.fitpath.ui.model.StepsUiState,
    onEnable: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.steps_title), style = MaterialTheme.typography.titleMedium)
            when (stepsUi.status) {
                StepsStatus.Disabled, StepsStatus.PermissionNeeded -> {
                    Text(stringResource(R.string.steps_permission_needed), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onEnable) {
                        Text(stringResource(R.string.steps_enable))
                    }
                }
                StepsStatus.Unsupported -> {
                    Text(stringResource(R.string.steps_not_supported), style = MaterialTheme.typography.bodyMedium)
                }
                StepsStatus.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.steps_loading))
                    }
                }
                StepsStatus.Ready -> {
                    StepRing(steps = stepsUi.stepsToday, goal = stepsUi.goal)
                }
            }
        }
    }
}

@Composable
private fun StepRing(steps: Int, goal: Int) {
    val progress = if (goal > 0) min(1f, steps.toFloat() / goal.toFloat()) else 0f
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.height(120.dp).fillMaxWidth()) {
            val diameter = size.minDimension
            val strokeWidth = 16.dp.toPx()
            val radius = diameter / 2f - strokeWidth
            val center = Offset(size.width / 2f, size.height / 2f)
            drawArc(
                color = backgroundColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            drawArc(
                color = progressColor,
                startAngle = 135f,
                sweepAngle = 270f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }
        Text(stringResource(R.string.steps_progress, steps, goal), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ErrorCard(onEditProfile: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.error_invalid_input))
            OutlinedButton(onClick = onEditProfile) { Text(stringResource(R.string.edit_profile)) }
        }
    }
}

@Composable
private fun NeedOnboardingCard(onEditProfile: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.onboarding_needed))
            OutlinedButton(onClick = onEditProfile) { Text(stringResource(R.string.edit_profile)) }
        }
    }
}

@Composable
private fun PlanSummary(plan: TodayPlan) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(plan.explanation)
            Divider()
            Text("${stringResource(R.string.calorie_range)}: ${plan.calories.min}–${plan.calories.max} kcal")
            Text("${stringResource(R.string.macro_ranges)}:")
            Text(" • ${stringResource(R.string.carbs)}: ${plan.macros.carbsG.min}–${plan.macros.carbsG.max} g")
            Text(" • ${stringResource(R.string.protein)}: ${plan.macros.proteinG.min}–${plan.macros.proteinG.max} g")
            Text(" • ${stringResource(R.string.fat)}: ${plan.macros.fatG.min}–${plan.macros.fatG.max} g")
        }
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
                meal.substitutions.forEach { sub ->
                    AssistChip(onClick = { /* Step3+: wire swap into state */ }, label = { Text(sub.title) })
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
                plan.workout.substitutions.forEach {
                    AssistChip(onClick = { /* Step3+: wire swap into state */ }, label = { Text(it.title) })
                }
            }
        }
    }
}

@Composable private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)
