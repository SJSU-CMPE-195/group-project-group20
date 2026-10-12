package com.fitfuel.app.data.repository

import com.fitfuel.app.model.Meal
import com.fitfuel.app.model.TagPreference
import kotlinx.coroutines.flow.Flow

// 195B Weeks 5-6: Repository contract keeps preference learning independent from Room implementation details.
interface TagPreferenceRepository {
    val preferences: Flow<Map<String, TagPreference>>

    suspend fun currentPreferences(): Map<String, TagPreference>

    suspend fun recordSavedMeal(meal: Meal)

    suspend fun recordDismissedMeal(meal: Meal)
}
