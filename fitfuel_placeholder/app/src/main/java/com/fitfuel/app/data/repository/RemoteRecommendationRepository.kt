package com.fitfuel.app.data.repository

import com.fitfuel.app.data.DemoMeals
import com.fitfuel.app.data.remote.RecommendationApi
import com.fitfuel.app.data.remote.buildRecommendationRequest
import com.fitfuel.app.data.remote.toDomain
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.RecommendationResult
import com.fitfuel.app.model.TagPreference
import com.fitfuel.app.recommendation.RecommendationEngine
import java.time.LocalDate
import kotlinx.coroutines.CancellationException

// 195B Weeks 3-4: Uses the backend when available and preserves a local fallback for offline development.
class RemoteRecommendationRepository(
    private val api: RecommendationApi
) : RecommendationRepository {
    override suspend fun getRecommendations(
        date: LocalDate,
        dayEntry: DayEntry?,
        // 195B Weeks 5-6: Preference map is sent to FastAPI and reused by the Android fallback path.
        preferences: Map<String, TagPreference>
    ): RecommendationResult {
        return try {
            api.getRecommendations(
                buildRecommendationRequest(
                    date = date.toString(),
                    dayEntry = dayEntry,
                    meals = DemoMeals.all,
                    preferences = preferences
                )
            ).toDomain()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            RecommendationResult(
                // 195B Weeks 5-6: Offline scoring uses the same learned tag affinities as the backend.
                recommendations = RecommendationEngine.getRecommendations(dayEntry, preferences),
                source = "android_fallback",
                message = "Backend unavailable; showing personalized recommendations generated on-device."
            )
        }
    }
}
