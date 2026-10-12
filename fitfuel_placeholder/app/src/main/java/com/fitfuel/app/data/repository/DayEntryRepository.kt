package com.fitfuel.app.data.repository

import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// 195B Weeks 1-2: Repository contract keeps UI state independent from Room APIs.
interface DayEntryRepository {
    val entries: Flow<Map<LocalDate, DayEntry>>

    suspend fun save(date: LocalDate, entry: DayEntry)

    suspend fun addMeal(date: LocalDate, meal: Meal)
}
