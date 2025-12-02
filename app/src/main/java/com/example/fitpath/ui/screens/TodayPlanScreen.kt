// File: app/src/main/java/com/fitpath/ui/screens/TodayPlanScreen.kt
package com.example.fitpath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import com.example.fitpath.domain.plan.MealSuggestion
import com.example.fitpath.domain.plan.TodayPlan
import com.example.fitpath.ui.vm.AppViewModel

@Composable
fun TodayPlanScreen(
    vm: AppViewModel,
    onEditProfile: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    val plan = ui.plan

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.today_title), style = MaterialTheme.typography.headlineSmall)

        if (ui.planError) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.error_invalid_input))
                    OutlinedButton(onClick = onEditProfile) { Text(stringResource(R.string.edit_profile)) }
                }
            }
            return
        }

        if (plan == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Complete onboarding to generate your plan.")
                    OutlinedButton(onClick = onEditProfile) { Text(stringResource(R.string.edit_profile)) }
                }
            }
            return
        }

        PlanSummary(plan)
        MealBlock(title = stringResource(R.string.breakfast), meal = plan.breakfast)
        MealBlock(title = stringResource(R.string.lunch), meal = plan.lunch)
        MealBlock(title = stringResource(R.string.dinner), meal = plan.dinner)
        WorkoutBlock(plan = plan, done = ui.workoutDone, onToggle = { vm.toggleWorkoutDone() })

        Text(text = stringResource(R.string.safety_note), style = MaterialTheme.typography.bodySmall)
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
