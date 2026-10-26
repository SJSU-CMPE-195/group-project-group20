package com.fitfuel.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitfuel.app.data.repository.OfflineDayEntryRepository
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

// 195B Weeks 7-8 (v0.5.0): Repository integration test verifies recommended-meal nutrition is atomically reflected in persistent daily totals.
@RunWith(AndroidJUnit4::class)
class OfflineDayEntryRepositoryTest {
    private lateinit var database: FitFuelDatabase
    private lateinit var repository: OfflineDayEntryRepository

    @Before
    fun createRepository() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FitFuelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OfflineDayEntryRepository(database.dayEntryDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun addMealUpdatesPersistedCaloriesProteinAndRemainingTargets() = runBlocking {
        val date = LocalDate.of(2026, 8, 14)
        repository.save(
            date,
            DayEntry(
                caloriesEaten = "1000",
                proteinEaten = "70",
                calorieTarget = 2200,
                remainingCalories = 1200,
                proteinTarget = 150,
                remainingProtein = 80
            )
        )

        repository.addMeal(
            date,
            Meal(
                id = "test_meal",
                name = "Test Meal",
                calories = 500,
                protein = 30,
                tags = listOf("test"),
                emoji = "T",
                description = "Test"
            )
        )

        val saved = repository.entries.first().getValue(date)
        assertEquals("1500", saved.caloriesEaten)
        assertEquals("100", saved.proteinEaten)
        assertEquals(700, saved.remainingCalories)
        assertEquals(50, saved.remainingProtein)
    }
}
