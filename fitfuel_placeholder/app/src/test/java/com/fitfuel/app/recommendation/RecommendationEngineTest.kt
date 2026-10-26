package com.fitfuel.app.recommendation

import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.TagPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

// 195B Weeks 7-8 (v0.5.0): Local fallback tests guard recommendation behavior when FastAPI/OpenAI is unavailable.
class RecommendationEngineTest {
    @Test
    fun gainMusclePrefersProteinDenseMeals() {
        val day = DayEntry(
            goal = "Gain Muscle",
            calorieTarget = 2600,
            caloriesEaten = "1900",
            remainingCalories = 700,
            proteinTarget = 180,
            proteinEaten = "130",
            remainingProtein = 50
        )

        val result = RecommendationEngine.getRecommendations(day)

        assertEquals(3, result.size)
        assertEquals("filet_mignon", result.first().meal.id)
    }

    @Test
    fun strongNegativeTagIsFilteredWhenAlternativesExist() {
        val preferences = mapOf(
            "italian" to TagPreference(
                tag = "italian",
                affinityScore = -6,
                positiveSignals = 0,
                negativeSignals = 3,
                lastUpdatedEpochMillis = 0L
            )
        )
        val day = DayEntry(
            goal = "Maintain",
            calorieTarget = 2400,
            caloriesEaten = "1600",
            remainingCalories = 800,
            proteinTarget = 150,
            proteinEaten = "100",
            remainingProtein = 50
        )

        val result = RecommendationEngine.getRecommendations(day, preferences)

        assertEquals(3, result.size)
        assertFalse(result.any { "italian" in it.meal.tags })
    }
}
