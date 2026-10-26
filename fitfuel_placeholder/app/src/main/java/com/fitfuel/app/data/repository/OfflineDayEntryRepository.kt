package com.fitfuel.app.data.repository

import com.fitfuel.app.data.local.DayEntryDao
import com.fitfuel.app.data.local.toEntity
import com.fitfuel.app.data.local.toModel
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

// 195B Weeks 1-2: Room-backed repository used as the app's persistent source of truth.
class OfflineDayEntryRepository(
    private val dayEntryDao: DayEntryDao
) : DayEntryRepository {
    override val entries: Flow<Map<LocalDate, DayEntry>> =
        dayEntryDao.observeAll().map { entities ->
            entities.associate { entity ->
                LocalDate.parse(entity.date) to entity.toModel()
            }
        }

    override suspend fun save(date: LocalDate, entry: DayEntry) {
        dayEntryDao.upsert(entry.toEntity(date))
    }

    override suspend fun addMeal(date: LocalDate, meal: Meal) {
        val current = dayEntryDao.getByDate(date.toString())?.toModel() ?: DayEntry()
        val updatedCaloriesEaten = (current.caloriesEaten.toIntOrNull() ?: 0) + meal.calories
        val updatedProteinEaten = (current.proteinEaten.toIntOrNull() ?: 0) + meal.protein

        save(
            date = date,
            entry = current.copy(
                caloriesEaten = updatedCaloriesEaten.toString(),
                proteinEaten = updatedProteinEaten.toString(),
                remainingCalories = current.calorieTarget?.minus(updatedCaloriesEaten),
                remainingProtein = current.proteinTarget?.minus(updatedProteinEaten)
            )
        )
    }
}
