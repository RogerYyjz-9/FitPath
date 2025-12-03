// File: app/src/main/java/com/example/fitpath/domain/plan/MealTemplates.kt
package com.example.fitpath.domain.plan

data class MealTemplate(
    val id: String,
    val title: String,
    val description: String,
    val approxCalories: Int,
    val tags: Set<MealTag>
) {
    fun toSuggestion(substitutions: List<MealSuggestion> = emptyList()): MealSuggestion =
        MealSuggestion(
            title = title,
            description = description,
            approxCalories = approxCalories,
            tags = tags,
            substitutions = substitutions
        )
}

object MealTemplates {
    fun all(): List<MealTemplate> = listOf(
        MealTemplate(
            id = "oats_greek",
            title = "Greek yogurt oats",
            description = "Oats + Greek yogurt + berries + nuts (balanced, easy).",
            approxCalories = 450,
            tags = setOf(MealTag.BALANCED, MealTag.HIGH_PROTEIN, MealTag.VEGETARIAN, MealTag.NO_BEEF, MealTag.NO_PORK)
        ),
        MealTemplate(
            id = "eggs_toast",
            title = "Eggs & toast",
            description = "2 eggs + wholegrain toast + fruit.",
            approxCalories = 480,
            tags = setOf(MealTag.BALANCED, MealTag.HIGH_PROTEIN, MealTag.NO_BEEF, MealTag.NO_PORK)
        ),
        MealTemplate(
            id = "tofu_bowl",
            title = "Tofu rice bowl",
            description = "Tofu + rice + mixed veggies (simple vegetarian bowl).",
            approxCalories = 650,
            tags = setOf(MealTag.BALANCED, MealTag.VEGETARIAN, MealTag.NO_BEEF, MealTag.NO_PORK, MealTag.HALAL)
        ),
        MealTemplate(
            id = "chicken_salad",
            title = "Chicken salad wrap",
            description = "Chicken + salad + wrap (high protein).",
            approxCalories = 620,
            tags = setOf(MealTag.HIGH_PROTEIN, MealTag.NO_PORK, MealTag.NO_BEEF, MealTag.HALAL, MealTag.LOWER_FAT)
        ),
        MealTemplate(
            id = "salmon_veg",
            title = "Salmon + veggies",
            description = "Salmon + veggies + small rice portion.",
            approxCalories = 720,
            tags = setOf(MealTag.BALANCED, MealTag.HIGH_PROTEIN, MealTag.NO_PORK, MealTag.NO_BEEF, MealTag.HALAL)
        ),
        MealTemplate(
            id = "lentil_soup",
            title = "Lentil soup + bread",
            description = "Lentil soup + wholegrain bread (warm & filling).",
            approxCalories = 560,
            tags = setOf(MealTag.BALANCED, MealTag.VEGETARIAN, MealTag.NO_BEEF, MealTag.NO_PORK, MealTag.HALAL, MealTag.LOWER_FAT)
        ),
        MealTemplate(
            id = "beef_bowl",
            title = "Beef veggie bowl",
            description = "Lean beef + veggies + rice (balanced).",
            approxCalories = 750,
            tags = setOf(MealTag.BALANCED, MealTag.HIGH_PROTEIN, MealTag.NO_PORK)
        ),
        MealTemplate(
            id = "pork_noodles",
            title = "Pork noodles",
            description = "Pork noodles + greens (comfort food).",
            approxCalories = 780,
            tags = setOf(MealTag.BALANCED, MealTag.NO_BEEF)
        )
    )
}
