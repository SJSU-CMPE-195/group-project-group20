package com.fitfuel.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// 195B Weeks 7-8 (v0.5.0): Instrumented Room tests verify persistence and replacement behavior against a real SQLite engine.
@RunWith(AndroidJUnit4::class)
class DayEntryDaoTest {
    private lateinit var database: FitFuelDatabase
    private lateinit var dao: DayEntryDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FitFuelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.dayEntryDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun upsertPersistsAndEmitsDayEntry() = runBlocking {
        val entry = sampleEntry(caloriesEaten = "1400", proteinEaten = "95")
        dao.upsert(entry)

        assertEquals(entry, dao.getByDate("2026-08-14"))
        assertEquals(listOf(entry), dao.observeAll().first())
    }

    @Test
    fun upsertSameDateReplacesPriorValues() = runBlocking {
        dao.upsert(sampleEntry(caloriesEaten = "1000", proteinEaten = "70"))
        dao.upsert(sampleEntry(caloriesEaten = "1500", proteinEaten = "110"))

        val stored = dao.getByDate("2026-08-14")
        assertNotNull(stored)
        assertEquals("1500", stored?.caloriesEaten)
        assertEquals("110", stored?.proteinEaten)
        assertEquals(1, dao.observeAll().first().size)
    }

    private fun sampleEntry(caloriesEaten: String, proteinEaten: String) = DayEntryEntity(
        date = "2026-08-14",
        weight = "80",
        height = "180",
        age = "25",
        caloriesBurned = "300",
        caloriesEaten = caloriesEaten,
        proteinEaten = proteinEaten,
        sex = "Male",
        goal = "Maintain",
        calorieTarget = 2105,
        remainingCalories = 605,
        proteinTarget = 128,
        remainingProtein = 18
    )
}
