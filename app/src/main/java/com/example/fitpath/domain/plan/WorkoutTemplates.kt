// File: app/src/main/java/com/example/fitpath/domain/plan/WorkoutTemplates.kt
package com.example.fitpath.domain.plan

data class WorkoutTemplate(
    val id: String,
    val title: String,
    val details: String,
    val intensity: WorkoutIntensity
) {
    fun toSuggestion(subs: List<WorkoutSuggestion> = emptyList()): WorkoutSuggestion =
        WorkoutSuggestion(title = title, details = details, intensity = intensity, substitutions = subs)
}

object WorkoutTemplates {
    fun all(): List<WorkoutTemplate> = listOf(
        WorkoutTemplate(
            id = "walk_30",
            title = "Walk 30 minutes",
            details = "Easy pace. Aim for light sweating, able to talk.",
            intensity = WorkoutIntensity.LOW
        ),
        WorkoutTemplate(
            id = "mobility_12",
            title = "Mobility 12 minutes",
            details = "Neck/shoulders/hips + gentle stretches. No pain.",
            intensity = WorkoutIntensity.LOW
        ),
        WorkoutTemplate(
            id = "strength_20",
            title = "Strength 20 minutes",
            details = "3 rounds: squats, push-ups (or incline), rows (band), plank.",
            intensity = WorkoutIntensity.MODERATE
        ),
        WorkoutTemplate(
            id = "interval_16",
            title = "Intervals 16 minutes",
            details = "8 rounds: 40s brisk + 80s easy. Stop if dizzy or painful.",
            intensity = WorkoutIntensity.HIGH
        )
    )
}
