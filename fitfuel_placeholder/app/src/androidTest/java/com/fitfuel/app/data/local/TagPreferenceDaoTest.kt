package com.fitfuel.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// 195B Weeks 7-8 (v0.5.0): Database tests verify that preference signals accumulate, normalize, and remain bounded.
@RunWith(AndroidJUnit4::class)
class TagPreferenceDaoTest {
    private lateinit var database: FitFuelDatabase
    private lateinit var dao: TagPreferenceDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FitFuelDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.tagPreferenceDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun positiveAndNegativeSignalsUpdateAffinity() = runBlocking {
        dao.recordPositive(listOf("Italian", "savory", "italian"))
        dao.recordPositive(listOf("italian"))
        dao.recordNegative(listOf("savory"))

        val stored = dao.getAll().associateBy { it.tag }
        assertEquals(4, stored.getValue("italian").affinityScore)
        assertEquals(2, stored.getValue("italian").positiveSignals)
        assertEquals(0, stored.getValue("italian").negativeSignals)
        assertEquals(0, stored.getValue("savory").affinityScore)
        assertEquals(1, stored.getValue("savory").positiveSignals)
        assertEquals(1, stored.getValue("savory").negativeSignals)
    }

    @Test
    fun affinityIsClampedToConfiguredBounds() = runBlocking {
        repeat(20) { dao.recordPositive(listOf("quick")) }
        repeat(30) { dao.recordNegative(listOf("spicy")) }

        val stored = dao.getAll().associateBy { it.tag }
        assertEquals(20, stored.getValue("quick").affinityScore)
        assertEquals(-20, stored.getValue("spicy").affinityScore)
    }
}
