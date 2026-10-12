package com.fitfuel.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// 195B Weeks 5-6: DAO records positive and negative feedback without exposing SQL to the UI layer.
@Dao
abstract class TagPreferenceDao {
    @Query("SELECT * FROM tag_preferences ORDER BY affinityScore DESC, tag ASC")
    abstract fun observeAll(): Flow<List<TagPreferenceEntity>>

    @Query("SELECT * FROM tag_preferences ORDER BY affinityScore DESC, tag ASC")
    abstract suspend fun getAll(): List<TagPreferenceEntity>

    @Query("SELECT * FROM tag_preferences WHERE tag = :tag LIMIT 1")
    protected abstract suspend fun getByTag(tag: String): TagPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsert(preference: TagPreferenceEntity)

    // 195B Weeks 5-6: Saving a recommended meal is treated as a positive preference signal for every meal tag.
    @Transaction
    open suspend fun recordPositive(tags: List<String>) {
        recordSignal(tags = tags, affinityDelta = 2, positive = true)
    }

    // 195B Weeks 5-6: Explicit "Not for me" feedback lowers affinity for every tag attached to that meal.
    @Transaction
    open suspend fun recordNegative(tags: List<String>) {
        recordSignal(tags = tags, affinityDelta = -2, positive = false)
    }

    private suspend fun recordSignal(
        tags: List<String>,
        affinityDelta: Int,
        positive: Boolean
    ) {
        val timestamp = System.currentTimeMillis()
        tags.asSequence()
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { tag ->
                val current = getByTag(tag)
                upsert(
                    TagPreferenceEntity(
                        tag = tag,
                        affinityScore = ((current?.affinityScore ?: 0) + affinityDelta).coerceIn(-20, 20),
                        positiveSignals = (current?.positiveSignals ?: 0) + if (positive) 1 else 0,
                        negativeSignals = (current?.negativeSignals ?: 0) + if (positive) 0 else 1,
                        lastUpdatedEpochMillis = timestamp
                    )
                )
            }
    }
}
