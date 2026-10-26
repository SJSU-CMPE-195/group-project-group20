package com.fitfuel.app.data.repository

import com.fitfuel.app.data.local.TagPreferenceDao
import com.fitfuel.app.data.local.toModel
import com.fitfuel.app.model.Meal
import com.fitfuel.app.model.TagPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 195B Weeks 5-6: Room-backed repository persists learned tag affinities across app restarts.
class OfflineTagPreferenceRepository(
    private val tagPreferenceDao: TagPreferenceDao
) : TagPreferenceRepository {
    override val preferences: Flow<Map<String, TagPreference>> =
        tagPreferenceDao.observeAll().map { entities ->
            entities.associate { entity -> entity.tag to entity.toModel() }
        }

    override suspend fun currentPreferences(): Map<String, TagPreference> =
        tagPreferenceDao.getAll().associate { entity -> entity.tag to entity.toModel() }

    override suspend fun recordSavedMeal(meal: Meal) {
        tagPreferenceDao.recordPositive(meal.tags)
    }

    override suspend fun recordDismissedMeal(meal: Meal) {
        tagPreferenceDao.recordNegative(meal.tags)
    }
}
