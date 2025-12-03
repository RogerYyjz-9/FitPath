// File: app/src/main/java/com/example/fitpath/domain/plan/PlanValidator.kt
package com.example.fitpath.domain.plan

import com.example.fitpath.domain.model.Sex

data class ValidationResult(val ok: Boolean, val reason: String? = null)

object PlanValidator {

    // Conservative minimum intake guardrails (not medical advice).
    fun minIntakeBySex(sex: Sex): Int = when (sex) {
        Sex.MALE -> 1500
        Sex.FEMALE -> 1200
        Sex.UNSPECIFIED -> 1200
    }

    fun validateCaloriesRange(range: Range, sex: Sex): ValidationResult {
        val minAllowed = minIntakeBySex(sex)
        if (range.max < minAllowed) return ValidationResult(false, "Below minimum intake")
        if (range.min < 900) return ValidationResult(false, "Extremely low")
        if (range.max > 4500) return ValidationResult(false, "Extremely high")
        if (range.max - range.min > 1200) return ValidationResult(false, "Too wide")
        return ValidationResult(true)
    }

    fun validateMacros(macros: MacroRanges): ValidationResult {
        if (macros.carbsG.min < 0 || macros.proteinG.min < 0 || macros.fatG.min < 0) {
            return ValidationResult(false, "Negative macros")
        }
        return ValidationResult(true)
    }
}
