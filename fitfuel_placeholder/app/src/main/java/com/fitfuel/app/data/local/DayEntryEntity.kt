package com.fitfuel.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// 195B Weeks 1-2: Room representation of a saved daily nutrition entry.
@Entity(tableName = "day_entries")
data class DayEntryEntity(
    @PrimaryKey val date: String,
    val weight: String,
    val height: String,
    val age: String,
    val caloriesBurned: String,
    val caloriesEaten: String,
    val proteinEaten: String,
    val sex: String,
    val goal: String,
    val calorieTarget: Int?,
    val remainingCalories: Int?,
    val proteinTarget: Int?,
    val remainingProtein: Int?
)
