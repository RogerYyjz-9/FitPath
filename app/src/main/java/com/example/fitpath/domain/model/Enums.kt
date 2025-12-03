// File: app/src/main/java/com/example/fitpath/domain/model/Enums.kt
package com.example.fitpath.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class LanguageMode { SYSTEM, EN, ZH }

enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2),
    LIGHT(1.375),
    MODERATE(1.55),
    ACTIVE(1.725),
    VERY_ACTIVE(1.9),
}

enum class FoodPreference {
    NONE, VEGETARIAN, HALAL, NO_BEEF, NO_PORK, HIGH_PROTEIN
}

enum class Sex { MALE, FEMALE, UNSPECIFIED }
