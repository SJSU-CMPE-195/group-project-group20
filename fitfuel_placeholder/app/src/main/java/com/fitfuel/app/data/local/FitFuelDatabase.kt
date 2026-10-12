package com.fitfuel.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 195B Weeks 1-2: Persistent Room database for FitFuel day entries.
@Database(
    // 195B Weeks 5-6: Tag preferences share the existing Room database so personalization survives restarts.
    entities = [DayEntryEntity::class, TagPreferenceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FitFuelDatabase : RoomDatabase() {
    abstract fun dayEntryDao(): DayEntryDao

    // 195B Weeks 5-6: Preference DAO exposes learned tag signals to the repository layer.
    abstract fun tagPreferenceDao(): TagPreferenceDao

    companion object {
        // 195B Weeks 5-6: Non-destructive migration preserves existing daily history while adding preference storage.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tag_preferences (
                        tag TEXT NOT NULL PRIMARY KEY,
                        affinityScore INTEGER NOT NULL,
                        positiveSignals INTEGER NOT NULL,
                        negativeSignals INTEGER NOT NULL,
                        lastUpdatedEpochMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Volatile
        private var instance: FitFuelDatabase? = null

        fun getInstance(context: Context): FitFuelDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FitFuelDatabase::class.java,
                    "fitfuel.db"
                )
                    // 195B Weeks 5-6: Existing version-1 installs upgrade without deleting saved day entries.
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }
    }
}
