// File: app/src/main/java/com/example/fitpath/domain/plan/PlanModels.kt
package com.example.fitpath.domain.plan

data class Range(val min: Int, val max: Int) {
    init { require(min <= max) }
    fun clamp(minAllowed: Int, maxAllowed: Int): Range {
        val nmin = min.coerceAtLeast(minAllowed).coerceAtMost(maxAllowed)
        val nmax = max.coerceAtLeast(minAllowed).coerceAtMost(maxAllowed)
        return if (nmin <= nmax) Range(nmin, nmax) else Range(nmin, nmin)
    }
}

data class MacroRanges(
    val carbsG: Range,
    val proteinG: Range,
    val fatG: Range
)

enum class GoalType { LOSE, MAINTAIN, GAIN }

data class MealSuggestion(
    val title: String,
    val description: String,
    val approxCalories: Int,
    val tags: Set<MealTag>,
    val substitutions: List<MealSuggestion> = emptyList()
)

enum class MealTag {
    BALANCED, HIGH_PROTEIN, VEGETARIAN, HALAL, NO_PORK, NO_BEEF, LOWER_FAT
}

data class WorkoutSuggestion(
    val title: String,
    val details: String,
    val intensity: WorkoutIntensity,
    val substitutions: List<WorkoutSuggestion> = emptyList()
)

enum class WorkoutIntensity { LOW, MODERATE, HIGH }

data class TodayPlan(
    val goalType: GoalType,
    val bmr: Int,
    val tdee: Int,
    val calories: Range,
    val macros: MacroRanges,
    val explanation: String,
    val breakfast: MealSuggestion,
    val lunch: MealSuggestion,
    val dinner: MealSuggestion,
    val workout: WorkoutSuggestion,
    val safetyNote: String
)
