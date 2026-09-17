package com.fitfuel.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 195B Weeks 1-2: Persistent Room database for FitFuel day entries.
@Database(
    entities = [DayEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FitFuelDatabase : RoomDatabase() {
    abstract fun dayEntryDao(): DayEntryDao

    companion object {
        @Volatile
        private var instance: FitFuelDatabase? = null

        fun getInstance(context: Context): FitFuelDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FitFuelDatabase::class.java,
                    "fitfuel.db"
                ).build().also { instance = it }
            }
    }
}
