package com.fitfuel.app.model

import androidx.compose.runtime.Immutable

// 195B Weeks 7-8 (v0.5.0): Immutable domain models let Compose skip avoidable recomposition when references are unchanged.
@Immutable
data class DayEntry(
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val caloriesBurned: String = "",
    val caloriesEaten: String = "",
    val proteinEaten: String = "",
    val sex: String = "Male",
    val goal: String = "Maintain",
    val calorieTarget: Int? = null,
    val remainingCalories: Int? = null,
    val proteinTarget: Int? = null,
    val remainingProtein: Int? = null
)
