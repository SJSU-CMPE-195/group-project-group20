package com.fitfuel.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// 195B Weeks 3-4: Retrofit interface for the Python FastAPI recommendation endpoint.
interface RecommendationApi {
    @POST("api/v1/recommendations")
    suspend fun getRecommendations(
        @Body request: RecommendationRequestDto
    ): RecommendationResponseDto
}
