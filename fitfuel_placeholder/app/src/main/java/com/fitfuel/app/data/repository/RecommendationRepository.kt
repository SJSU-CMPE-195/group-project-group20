package com.fitfuel.app.data.repository

import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.RecommendationResult
import com.fitfuel.app.model.TagPreference
import java.time.LocalDate

// 195B Weeks 3-4: Repository boundary keeps network concerns out of the ViewModel and Compose UI.
interface RecommendationRepository {
    suspend fun getRecommendations(
        date: LocalDate,
        dayEntry: DayEntry?,
        // 195B Weeks 5-6: Learned preferences travel through the same abstraction as nutrition context.
        preferences: Map<String, TagPreference> = emptyMap()
    ): RecommendationResult
}
