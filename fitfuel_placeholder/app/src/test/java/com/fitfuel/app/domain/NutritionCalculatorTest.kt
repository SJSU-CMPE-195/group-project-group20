package com.fitfuel.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// 195B Weeks 7-8 (v0.5.0): Unit coverage verifies BMR/goal math independently from the Android UI.
class NutritionCalculatorTest {
    @Test
    fun maleMaintainCalculationMatchesExpectedValue() {
        val result = NutritionCalculator.calculateCalories(
            weight = 80,
            height = 180,
            age = 25,
            burned = 300,
            goal = "Maintain",
            sex = "Male"
        )

        assertEquals(2105, result)
    }

    @Test
    fun femaleWeightLossAppliesFemaleBmrAndDeficit() {
        val result = NutritionCalculator.calculateCalories(
            weight = 65,
            height = 165,
            age = 30,
            burned = 250,
            goal = "Lose Weight",
            sex = "Female"
        )

        assertEquals(1220, result)
    }

    @Test
    fun gainMuscleProteinTargetUsesTwoGramsPerKg() {
        assertEquals(160, NutritionCalculator.calculateProtein(80, "Gain Muscle"))
    }

    @Test
    fun loseWeightProteinTargetUsesHigherMultiplier() {
        assertEquals(176, NutritionCalculator.calculateProtein(80, "Lose Weight"))
    }

    @Test
    fun missingRequiredBmrInputReturnsNull() {
        assertNull(
            NutritionCalculator.calculateCalories(
                weight = 80,
                height = null,
                age = 25,
                burned = 0,
                goal = "Maintain",
                sex = "Male"
            )
        )
    }
}
