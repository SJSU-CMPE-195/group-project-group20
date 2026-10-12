package com.fitfuel.app.recommendation

import com.fitfuel.app.data.DemoMeals
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import com.fitfuel.app.model.RecommendedMeal
import com.fitfuel.app.model.TagPreference
import kotlin.math.abs

object RecommendationEngine {

    fun getRecommendations(
        dayEntry: DayEntry?,
        // 195B Weeks 5-6: Local fallback accepts the same persisted tag affinities used by FastAPI/GPT.
        preferences: Map<String, TagPreference> = emptyMap()
    ): List<RecommendedMeal> {
        // 195B Weeks 5-6: Strong dislikes and calorie bounds filter the catalog before ranking whenever three choices remain.
        val meals = filterCandidates(DemoMeals.all, dayEntry, preferences)

        if (dayEntry == null || dayEntry.calorieTarget == null || dayEntry.proteinTarget == null) {
            return meals.shuffled().take(3).map {
                RecommendedMeal(
                    meal = it,
                    reason = buildSampleReason(it, preferences)
                )
            }
        }

        val caloriesLeft = dayEntry.remainingCalories
            ?: (dayEntry.calorieTarget - (dayEntry.caloriesEaten.toIntOrNull() ?: 0))
        val proteinLeft = dayEntry.remainingProtein
            ?: (dayEntry.proteinTarget - (dayEntry.proteinEaten.toIntOrNull() ?: 0))
        val goal = dayEntry.goal

        return meals
            .map { meal ->
                val score = scoreMeal(meal, caloriesLeft, proteinLeft, goal, preferences)
                RecommendedMeal(
                    meal = meal,
                    reason = buildReason(meal, caloriesLeft, proteinLeft, goal, preferences)
                ) to score
            }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(3)
    }

    // 195B Weeks 5-6: Filtering respects explicit negative feedback first and hard calorie fit when enough alternatives exist.
    private fun filterCandidates(
        meals: List<Meal>,
        dayEntry: DayEntry?,
        preferences: Map<String, TagPreference>
    ): List<Meal> {
        val stronglyDislikedTags = preferences.values
            .filter { it.affinityScore <= -4 }
            .map { it.tag }
            .toSet()

        val preferenceSafeMeals = meals.filter { meal ->
            meal.tags.none { it.lowercase() in stronglyDislikedTags }
        }
        val preferenceFiltered = if (preferenceSafeMeals.size >= 3) preferenceSafeMeals else meals

        val caloriesLeft = dayEntry?.remainingCalories
            ?: dayEntry?.calorieTarget?.minus(dayEntry.caloriesEaten.toIntOrNull() ?: 0)

        if (caloriesLeft != null) {
            val withinBudget = preferenceFiltered.filter { it.calories <= caloriesLeft.coerceAtLeast(0) }
            if (withinBudget.size >= 3) return withinBudget
        }

        return preferenceFiltered
    }

    private fun scoreMeal(
        meal: Meal,
        caloriesLeft: Int,
        proteinLeft: Int,
        goal: String,
        // 195B Weeks 5-6: Learned affinity adjusts ranking only after nutrition-fit scoring has been applied.
        preferences: Map<String, TagPreference>
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

        // 195B Weeks 5-6: Positive tags boost similar meals while negative tags reduce their rank without overriding hard filters.
        score += meal.tags.sumOf { tag ->
            preferences[tag.lowercase()]?.affinityScore ?: 0
        } * 25.0

        return score
    }

    private fun buildReason(
        meal: Meal,
        caloriesLeft: Int,
        proteinLeft: Int,
        goal: String,
        // 195B Weeks 5-6: Recommendation copy can explain when a meal matches learned preferences.
        preferences: Map<String, TagPreference>
    ): String {
        val calorieFit = if (meal.calories <= caloriesLeft) {
            "fits within today's remaining calories"
        } else {
            "is slightly above today's remaining calories but is kept for comparison"
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

        // 195B Weeks 5-6: Only positive learned tags are surfaced as personalization rationale.
        val matchedPreferences = meal.tags
            .filter { tag -> (preferences[tag.lowercase()]?.affinityScore ?: 0) > 0 }
            .sortedByDescending { tag -> preferences[tag.lowercase()]?.affinityScore ?: 0 }
            .take(2)

        val preferenceNote = if (matchedPreferences.isEmpty()) {
            ""
        } else {
            " It also matches your learned preferences for ${matchedPreferences.joinToString(" and ")}."
        }

        return "${meal.name} $calorieFit and $proteinFit. $goalNote$preferenceNote"
    }

    // 195B Weeks 5-6: Sample mode can still acknowledge prior preferences when a selected day has no nutrition targets.
    private fun buildSampleReason(
        meal: Meal,
        preferences: Map<String, TagPreference>
    ): String {
        val matchedPreferences = meal.tags
            .filter { tag -> (preferences[tag.lowercase()]?.affinityScore ?: 0) > 0 }
            .take(2)
        return if (matchedPreferences.isEmpty()) {
            "Sample recommendation shown until daily calorie and protein targets are saved."
        } else {
            "Sample recommendation that also matches your learned preferences for ${matchedPreferences.joinToString(" and ")}."
        }
    }
}
