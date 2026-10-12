package com.fitfuel.app.ui.state

import com.fitfuel.app.model.RecommendedMeal

// 195B Weeks 3-4: Tracks asynchronous backend recommendation loading independently from persisted day data.
data class RecommendationUiState(
    val recommendations: List<RecommendedMeal> = emptyList(),
    val isLoading: Boolean = false,
    val source: String? = null,
    val message: String? = null
)
