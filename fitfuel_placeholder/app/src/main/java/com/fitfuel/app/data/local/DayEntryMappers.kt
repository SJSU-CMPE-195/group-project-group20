package com.fitfuel.app.data.local

import com.fitfuel.app.model.DayEntry
import java.time.LocalDate

// 195B Weeks 1-2: Converts between the app model and the Room entity.
fun DayEntryEntity.toModel(): DayEntry = DayEntry(
    weight = weight,
    height = height,
    age = age,
    caloriesBurned = caloriesBurned,
    caloriesEaten = caloriesEaten,
    proteinEaten = proteinEaten,
    sex = sex,
    goal = goal,
    calorieTarget = calorieTarget,
    remainingCalories = remainingCalories,
    proteinTarget = proteinTarget,
    remainingProtein = remainingProtein
)

fun DayEntry.toEntity(date: LocalDate): DayEntryEntity = DayEntryEntity(
    date = date.toString(),
    weight = weight,
    height = height,
    age = age,
    caloriesBurned = caloriesBurned,
    caloriesEaten = caloriesEaten,
    proteinEaten = proteinEaten,
    sex = sex,
    goal = goal,
    calorieTarget = calorieTarget,
    remainingCalories = remainingCalories,
    proteinTarget = proteinTarget,
    remainingProtein = remainingProtein
)
