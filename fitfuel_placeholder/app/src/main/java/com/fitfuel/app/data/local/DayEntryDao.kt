package com.fitfuel.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// 195B Weeks 1-2: Database access for observing and updating saved days.
@Dao
interface DayEntryDao {
    @Query("SELECT * FROM day_entries ORDER BY date ASC")
    fun observeAll(): Flow<List<DayEntryEntity>>

    @Query("SELECT * FROM day_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DayEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DayEntryEntity)
}
