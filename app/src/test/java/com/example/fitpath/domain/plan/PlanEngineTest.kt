package com.example.fitpath.domain.plan

import com.example.fitpath.domain.model.ActivityLevel
import com.example.fitpath.domain.model.FoodPreference
import com.example.fitpath.domain.model.Sex
import com.example.fitpath.domain.profile.UserProfile
import org.junit.Test

class PlanEngineTest {

    @Test
    fun loseWeight_generates_safe_range() {
        val p = UserProfile(
            currentWeightKg = 80.0,
            targetWeightKg = 72.0,
            activityLevel = ActivityLevel.MODERATE,
            foodPreference = FoodPreference.NONE,
            sex = Sex.MALE,
            ageYears = 25
        )
        val plan = PlanEngine.generate(p).getOrThrow()
        val vr = PlanValidator.validateCaloriesRange(plan.calories, p.sex)
        assertTrue(vr.ok)
        assertTrue(plan.calories.min < plan.tdee)
        assertTrue(plan.calories.max < plan.tdee)
    }

    @Test
    fun maintain_generates_narrow_band() {
        val p = UserProfile(
            currentWeightKg = 65.0,
            targetWeightKg = 65.0,
            activityLevel = ActivityLevel.LIGHT,
            foodPreference = FoodPreference.NONE,
            sex = Sex.FEMALE,
            ageYears = 30
        )
        val plan = PlanEngine.generate(p).getOrThrow()
        assertTrue(plan.calories.max - plan.calories.min <= 250)
    }

    @Test
    fun gain_generates_surplus() {
        val p = UserProfile(
            currentWeightKg = 55.0,
            targetWeightKg = 60.0,
            activityLevel = ActivityLevel.ACTIVE,
            foodPreference = FoodPreference.HIGH_PROTEIN,
            sex = Sex.FEMALE,
            ageYears = 22
        )
        val plan = PlanEngine.generate(p).getOrThrow()
        assertTrue(plan.calories.min > plan.tdee)
    }

    @Test
    fun missing_sex_or_age_uses_fallback_and_still_safe() {
        val p = UserProfile(
            currentWeightKg = 70.0,
            targetWeightKg = 65.0,
            activityLevel = ActivityLevel.MODERATE,
            foodPreference = FoodPreference.NONE,
            sex = Sex.UNSPECIFIED,
            ageYears = null
        )
        val plan = PlanEngine.generate(p).getOrThrow()
        assertTrue(plan.bmr in 900..3000)
        assertTrue(PlanValidator.validateCaloriesRange(plan.calories, p.sex).ok)
    }

    @Test
    fun invalid_input_returns_failure() {
        val p = UserProfile(
            currentWeightKg = 20.0, // outside our constraints
            targetWeightKg = 18.0,
            activityLevel = ActivityLevel.MODERATE,
            foodPreference = FoodPreference.NONE,
            sex = Sex.MALE,
            ageYears = 25
        )
        val res = PlanEngine.generate(p)
        assertTrue(res.isFailure)
    }
}