package com.fitfuel.app.data

import com.fitfuel.app.model.Meal

/**
 * Proof-of-concept meals for the recommendation module.
 * These values are intentionally normalized for a stable demo.
 */
object DemoMeals {
    val caesarSalad = Meal(
        id = "caesar_salad",
        name = "Caesar Salad",
        calories = 350,
        protein = 12,
        tags = listOf("light", "salad", "lower-calorie"),
        emoji = "🥗",
        description = "A lighter option that fits tighter calorie budgets."
    )

    val carbonara = Meal(
        id = "carbonara",
        name = "Spaghetti Carbonara",
        calories = 500,
        protein = 18,
        tags = listOf("pasta", "comfort", "higher-calorie"),
        emoji = "🍝",
        description = "A richer pasta option with moderate protein and higher calories."
    )

    val filetMignon = Meal(
        id = "filet_mignon",
        name = "Filet Mignon",
        calories = 550,
        protein = 40,
        tags = listOf("high-protein", "meat", "premium"),
        emoji = "🥩",
        description = "A protein-forward option that fits muscle-focused days well."
    )

    val all = listOf(caesarSalad, carbonara, filetMignon)
}
