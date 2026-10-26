package com.fitfuel.app.model

// 195B Weeks 3-4: Identifies where a recommendation set came from for transparent fallback behavior.
data class RecommendationResult(
    val recommendations: List<RecommendedMeal>,
    val source: String,
    val message: String? = null
)
