package com.fitfuel.app.domain

// 195B Weeks 7-8 (v0.5.0): Pure nutrition calculations are isolated from Compose so they can be unit-tested without Android runtime dependencies.
object NutritionCalculator {
    fun calculateCalories(
        weight: Int?,
        height: Int?,
        age: Int?,
        burned: Int?,
        goal: String,
        sex: String
    ): Int? {
        if (weight == null || height == null || age == null) return null

        val bmr = when (sex) {
            "Female" -> ((10 * weight) + (6.25 * height) - (5 * age) - 161).toInt()
            else -> ((10 * weight) + (6.25 * height) - (5 * age) + 5).toInt()
        }

        val adjustment = when (goal) {
            "Lose Weight" -> -400
            "Gain Muscle" -> 300
            else -> 0
        }

        return bmr + (burned ?: 0) + adjustment
    }

    fun calculateProtein(weight: Int?, goal: String): Int? {
        if (weight == null) return null

        val multiplier = when (goal) {
            "Lose Weight" -> 2.2
            "Gain Muscle" -> 2.0
            else -> 1.6
        }

        return (weight * multiplier).toInt()
    }
}
