// File: app/src/main/java/com/example/fitpath/domain/plan/PlanEngine.kt
package com.example.fitpath.domain.plan

import com.example.fitpath.domain.model.ActivityLevel
import com.example.fitpath.domain.model.FoodPreference
import com.example.fitpath.domain.model.Sex
import com.example.fitpath.domain.profile.UserProfile
import kotlin.math.roundToInt

object PlanEngine {
    private const val KCAL_PER_KG_FAT = 7700 // common approximation

    fun generate(profile: UserProfile): Result<TodayPlan> = runCatching {
        require(profile.isReadyForPlan())

        val current = profile.currentWeightKg!!
        val target = profile.targetWeightKg!!
        require(current in 30.0..300.0 && target in 30.0..300.0)

        val goalType = when {
            target < current -> GoalType.LOSE
            target > current -> GoalType.GAIN
            else -> GoalType.MAINTAIN
        }

        val bmr = BmrEstimator.estimateKcalPerDay(
            weightKg = current,
            sex = profile.sex,
            ageYears = profile.ageYears
        )
        val tdee = (bmr * profile.activityLevel.factor).roundToInt()

        val calRange = when (goalType) {
            GoalType.LOSE -> {
                // 0.5–1.0 kg/week => ~550–1100 kcal/day deficit
                val minDeficit = (KCAL_PER_KG_FAT * 0.5 / 7.0).roundToInt()  // ~550
                val maxDeficit = (KCAL_PER_KG_FAT * 1.0 / 7.0).roundToInt()  // ~1100
                Range(tdee - maxDeficit, tdee - minDeficit)
            }
            GoalType.GAIN -> Range(tdee + 250, tdee + 500) // gentle surplus
            GoalType.MAINTAIN -> Range(tdee - 100, tdee + 100)
        }.clamp(
            minAllowed = PlanValidator.minIntakeBySex(profile.sex),
            maxAllowed = 4500
        )

        val vr = PlanValidator.validateCaloriesRange(calRange, profile.sex)
        if (!vr.ok) error("Unsafe calories: ${vr.reason}")

        val macros = calculateMacroRanges(calRange)
        val vm = PlanValidator.validateMacros(macros)
        if (!vm.ok) error("Unsafe macros: ${vm.reason}")

        val explanation = buildExplanation(goalType, profile.activityLevel, profile.sex, profile.ageYears != null)
        val safety = "This is not medical advice. If you feel unwell, stop and seek professional help."

        val allMeals = MealTemplates.all()
        val filteredMeals = filterMeals(allMeals, profile.foodPreference)

        val breakfast = pickMeal(filteredMeals, calRange, 0.30, profile.foodPreference == FoodPreference.HIGH_PROTEIN)
        val lunch = pickMeal(filteredMeals, calRange, 0.35, profile.foodPreference == FoodPreference.HIGH_PROTEIN)
        val dinner = pickMeal(filteredMeals, calRange, 0.35, profile.foodPreference == FoodPreference.HIGH_PROTEIN)

        val workout = pickWorkout(profile.activityLevel)

        TodayPlan(
            goalType = goalType,
            bmr = bmr,
            tdee = tdee,
            calories = calRange,
            macros = macros,
            explanation = explanation,
            breakfast = breakfast,
            lunch = lunch,
            dinner = dinner,
            workout = workout,
            safetyNote = safety
        )
    }

    private fun buildExplanation(goalType: GoalType, activity: ActivityLevel, sex: Sex, hasAge: Boolean): String {
        val goalText = when (goalType) {
            GoalType.LOSE -> "a gentle deficit"
            GoalType.GAIN -> "a gentle surplus"
            GoalType.MAINTAIN -> "maintenance"
        }
        val precision = if (sex == Sex.UNSPECIFIED || !hasAge) " (estimation is less precise—add sex/age for better accuracy)" else ""
        return "Based on your activity level (${activity.name.lowercase().replace('_', ' ')}) and today’s goal ($goalText), we propose a safe calorie range.$precision"
    }

    private fun calculateMacroRanges(calRange: Range): MacroRanges {
        // AMDR: Carbs 45–65%, Fat 20–35%, Protein 10–35%
        fun gramsRange(pMin: Double, pMax: Double, kcalPerGram: Int): Range {
            val gMin = (calRange.min * pMin / kcalPerGram).roundToInt()
            val gMax = (calRange.max * pMax / kcalPerGram).roundToInt()
            return Range(gMin.coerceAtLeast(0), gMax.coerceAtLeast(0))
        }
        return MacroRanges(
            carbsG = gramsRange(0.45, 0.65, 4),
            proteinG = gramsRange(0.10, 0.35, 4),
            fatG = gramsRange(0.20, 0.35, 9),
        )
    }

    private fun filterMeals(meals: List<MealTemplate>, pref: FoodPreference): List<MealTemplate> =
        meals.filter { t ->
            when (pref) {
                FoodPreference.NONE -> true
                FoodPreference.VEGETARIAN -> t.tags.contains(MealTag.VEGETARIAN)
                FoodPreference.HALAL -> t.tags.contains(MealTag.HALAL)
                FoodPreference.NO_BEEF -> t.tags.contains(MealTag.NO_BEEF)
                FoodPreference.NO_PORK -> t.tags.contains(MealTag.NO_PORK)
                FoodPreference.HIGH_PROTEIN -> true
            }
        }.ifEmpty { meals }

    private fun pickMeal(meals: List<MealTemplate>, dayCalories: Range, share: Double, preferHighProtein: Boolean): MealSuggestion {
        val mealRange = Range((dayCalories.min * share).roundToInt(), (dayCalories.max * share).roundToInt())
        val within = meals.filter { it.approxCalories in mealRange.min..mealRange.max }
        val candidates = if (within.isNotEmpty()) within else meals

        val primary = if (preferHighProtein) {
            candidates.sortedByDescending { it.tags.contains(MealTag.HIGH_PROTEIN) }.first()
        } else candidates.first()

        val substitutes = candidates.filter { it.id != primary.id }.take(4).map { it.toSuggestion() }
        return primary.toSuggestion(substitutions = substitutes)
    }

    private fun pickWorkout(activity: ActivityLevel): WorkoutSuggestion {
        val all = WorkoutTemplates.all()
        val primary = when (activity) {
            ActivityLevel.SEDENTARY, ActivityLevel.LIGHT -> all.first { it.intensity == WorkoutIntensity.LOW }
            ActivityLevel.MODERATE -> all.first { it.intensity == WorkoutIntensity.MODERATE }
            ActivityLevel.ACTIVE, ActivityLevel.VERY_ACTIVE -> all.first { it.intensity == WorkoutIntensity.HIGH }
        }
        val subs = all.filter { it.id != primary.id }.take(2).map { it.toSuggestion() }
        return primary.toSuggestion(subs)
    }
}
