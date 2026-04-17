package com.fitfuel.app.model

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
