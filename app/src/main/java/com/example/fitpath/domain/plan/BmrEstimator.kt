// File: app/src/main/java/com/example/fitpath/domain/plan/BmrEstimator.kt
package com.example.fitpath.domain.plan

import com.example.fitpath.domain.model.Sex

/**
 * BMR estimator without height (as requested).
 *
 * - If sex + age are provided: Schofield-style weight-only equations (kcal/day) by age band.
 * - If sex or age missing: a conservative fallback (~22 kcal/kg/day).
 *
 * NOTE: Rough consumer wellness estimate; not medical advice.
 */
object BmrEstimator {

    fun estimateKcalPerDay(weightKg: Double, sex: Sex, ageYears: Int?): Int {
        require(weightKg > 0)
        val w = weightKg

        if (sex == Sex.UNSPECIFIED || ageYears == null) {
            return (22.0 * w).toInt().coerceIn(900, 3000)
        }

        val age = ageYears
        val bmr = when (sex) {
            Sex.MALE -> maleSchofield(w, age)
            Sex.FEMALE -> femaleSchofield(w, age)
            Sex.UNSPECIFIED -> 22.0 * w
        }
        return bmr.toInt().coerceIn(900, 3500)
    }

    private fun maleSchofield(w: Double, age: Int): Double = when {
        age < 10 -> 22.7 * w + 495
        age < 18 -> 17.5 * w + 651
        age < 30 -> 15.3 * w + 679
        age < 60 -> 11.6 * w + 879
        else -> 13.5 * w + 487
    }

    private fun femaleSchofield(w: Double, age: Int): Double = when {
        age < 10 -> 22.5 * w + 499
        age < 18 -> 12.2 * w + 746
        age < 30 -> 14.7 * w + 496
        age < 60 -> 8.7 * w + 829
        else -> 10.5 * w + 596
    }
}
