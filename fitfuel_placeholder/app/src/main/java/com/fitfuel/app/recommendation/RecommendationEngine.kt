package com.fitfuel.app.recommendation

import com.fitfuel.app.data.DemoMeals
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import kotlin.math.abs

/**
 * Proof-of-concept GPT stub.
 * For now it ranks the 3 demo meals using the user's current day context.
 * Later this object can be replaced by a backend/OpenAI call.
 */
object RecommendationEngine {

    data class RecommendedMeal(
        val meal: Meal,
        val reason: String
    )

    fun getRecommendations(dayEntry: DayEntry?): List<RecommendedMeal> {
        val meals = DemoMeals.all

        if (dayEntry == null || dayEntry.calorieTarget == null || dayEntry.proteinTarget == null) {
            return meals.shuffled().take(3).map {
                RecommendedMeal(
                    meal = it,
                    reason = "Sample recommendation for the demo experience."
                )
            }
        }

        val caloriesLeft = dayEntry.remainingCalories ?: (dayEntry.calorieTarget - (dayEntry.caloriesEaten.toIntOrNull() ?: 0))
        val proteinLeft = dayEntry.remainingProtein ?: (dayEntry.proteinTarget - (dayEntry.proteinEaten.toIntOrNull() ?: 0))
        val goal = dayEntry.goal

        return meals
            .map { meal ->
                val score = scoreMeal(meal, caloriesLeft, proteinLeft, goal)
                RecommendedMeal(meal = meal, reason = buildReason(meal, caloriesLeft, proteinLeft, goal, score)) to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(3)
    }

    private fun scoreMeal(
        meal: Meal,
        caloriesLeft: Int,
        proteinLeft: Int,
        goal: String
    ): Double {
        var score = 0.0

        val calorieGap = abs(caloriesLeft - meal.calories)
        score += 300.0 - calorieGap.toDouble()

        when {
            meal.calories <= caloriesLeft -> score += 120.0
            else -> score -= 100.0
        }

        val proteinSupport = meal.protein.coerceAtMost(proteinLeft.coerceAtLeast(0))
        score += proteinSupport * 6.0

        when (goal) {
            "Lose Weight" -> {
                score -= meal.calories * 0.25
                score += meal.protein * 4.0
                if ("lower-calorie" in meal.tags) score += 45.0
            }
            "Gain Muscle" -> {
                score += meal.protein * 8.0
                score += meal.calories * 0.08
                if ("high-protein" in meal.tags) score += 60.0
            }
            else -> {
                score += meal.protein * 5.0
            }
        }

        return score
    }

    private fun buildReason(
        meal: Meal,
        caloriesLeft: Int,
        proteinLeft: Int,
        goal: String,
        score: Double
    ): String {
        val calorieFit = if (meal.calories <= caloriesLeft) {
            "fits within today's remaining calories"
        } else {
            "is slightly above today's remaining calories but is kept for demo comparison"
        }

        val proteinFit = when {
            proteinLeft <= 0 -> "protein is already on target"
            meal.protein >= proteinLeft -> "helps fully close the remaining protein gap"
            else -> "helps partially close the remaining protein gap"
        }

        val goalNote = when (goal) {
            "Lose Weight" -> "It is being ranked with a fat-loss bias."
            "Gain Muscle" -> "It is being ranked with a muscle-gain bias."
            else -> "It is being ranked for maintenance."
        }

        return "${meal.name} $calorieFit and $proteinFit. $goalNote Demo score: ${score.toInt()}."
    }
}
