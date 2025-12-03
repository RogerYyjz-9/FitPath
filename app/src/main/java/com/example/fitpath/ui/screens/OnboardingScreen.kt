// File: app/src/main/java/com/example/fitpath/ui/screens/OnboardingScreen.kt
package com.example.fitpath.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitpath.R
import com.example.fitpath.domain.model.ActivityLevel
import com.example.fitpath.domain.model.FoodPreference
import com.example.fitpath.domain.model.Sex
import com.example.fitpath.ui.vm.AppViewModel

@Composable
fun OnboardingScreen(
    vm: AppViewModel,
    onContinue: () -> Unit
) {
    val profile by vm.profile.collectAsState()

    var current by remember(profile.currentWeightKg) { mutableStateOf(profile.currentWeightKg?.toString() ?: "") }
    var target by remember(profile.targetWeightKg) { mutableStateOf(profile.targetWeightKg?.toString() ?: "") }
    var activity by remember(profile.activityLevel) { mutableStateOf(profile.activityLevel) }
    var pref by remember(profile.foodPreference) { mutableStateOf(profile.foodPreference) }
    var sex by remember(profile.sex) { mutableStateOf(profile.sex) }
    var age by remember(profile.ageYears) { mutableStateOf(profile.ageYears?.toString() ?: "") }
    // [新增] 身高状态
    var height by remember(profile.heightCm) { mutableStateOf(profile.heightCm?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = current,
            onValueChange = { current = it },
            label = { Text(stringResource(R.string.current_weight)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = target,
            onValueChange = { target = it },
            label = { Text(stringResource(R.string.target_weight)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        EnumDropdown(
            label = stringResource(R.string.activity_level),
            value = activity,
            values = ActivityLevel.entries.toList(),
            itemLabel = { activityLabel(it) },
            onChange = { activity = it }
        )

        EnumDropdown(
            label = stringResource(R.string.food_preference),
            value = pref,
            values = FoodPreference.entries.toList(),
            itemLabel = { prefLabel(it) },
            onChange = { pref = it }
        )

        EnumDropdown(
            label = stringResource(R.string.sex),
            value = sex,
            values = Sex.entries.toList(),
            itemLabel = { sexLabel(it) },
            onChange = { sex = it }
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = age,
            onValueChange = { age = it },
            label = { Text(stringResource(R.string.age_years)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // [新增] 身高输入框
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            onClick = {
                val c = current.toDoubleOrNull()
                val t = target.toDoubleOrNull()
                val a = age.toIntOrNull()?.takeIf { it in 5..120 }
                val h = height.toIntOrNull()?.takeIf { it in 50..250 } // 简单的范围校验

                vm.updateProfile(
                    currentWeightKg = c,
                    targetWeightKg = t,
                    activityLevel = activity,
                    foodPreference = pref,
                    sex = sex,
                    ageYears = a,
                    heightCm = h // [新增]
                )
                onContinue()
            }
        ) { Text(stringResource(R.string.continue_btn)) }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable private fun activityLabel(a: ActivityLevel): String = when (a) {
    ActivityLevel.SEDENTARY -> stringResource(R.string.activity_sedentary)
    ActivityLevel.LIGHT -> stringResource(R.string.activity_light)
    ActivityLevel.MODERATE -> stringResource(R.string.activity_moderate)
    ActivityLevel.ACTIVE -> stringResource(R.string.activity_active)
    ActivityLevel.VERY_ACTIVE -> stringResource(R.string.activity_very_active)
}

@Composable private fun prefLabel(p: FoodPreference): String = when (p) {
    FoodPreference.NONE -> stringResource(R.string.pref_none)
    FoodPreference.VEGETARIAN -> stringResource(R.string.pref_vegetarian)
    FoodPreference.HALAL -> stringResource(R.string.pref_halal)
    FoodPreference.NO_BEEF -> stringResource(R.string.pref_no_beef)
    FoodPreference.NO_PORK -> stringResource(R.string.pref_no_pork)
    FoodPreference.HIGH_PROTEIN -> stringResource(R.string.pref_high_protein)
}

@Composable private fun sexLabel(s: Sex): String = when (s) {
    Sex.UNSPECIFIED -> stringResource(R.string.sex_unspecified)
    Sex.MALE -> stringResource(R.string.sex_male)
    Sex.FEMALE -> stringResource(R.string.sex_female)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    value: T,
    values: List<T>,
    itemLabel: @Composable (T) -> String,
    onChange: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = itemLabel(value),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = { onChange(item); expanded = false }
                )
            }
        }
    }
}

@Composable private fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)