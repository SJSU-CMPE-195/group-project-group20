package com.fitfuel.app

import android.app.Application
import com.fitfuel.app.data.local.FitFuelDatabase
import com.fitfuel.app.data.repository.DayEntryRepository
import com.fitfuel.app.data.repository.OfflineDayEntryRepository

// 195B Weeks 1-2: Lightweight application container supplies persistent dependencies.
class FitFuelApplication : Application() {
    private val database by lazy { FitFuelDatabase.getInstance(this) }

    val dayEntryRepository: DayEntryRepository by lazy {
        OfflineDayEntryRepository(database.dayEntryDao())
    }
}
