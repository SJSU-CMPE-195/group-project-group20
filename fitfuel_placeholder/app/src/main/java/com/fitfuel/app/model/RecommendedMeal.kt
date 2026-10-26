package com.fitfuel.app.model

import androidx.compose.runtime.Immutable

// 195B Weeks 3-4: Stable domain model shared by local fallback and remote GPT recommendations.
// 195B Weeks 7-8 (v0.5.0): Immutable domain models let Compose skip avoidable recomposition when references are unchanged.
@Immutable
data class RecommendedMeal(
    val meal: Meal,
    val reason: String
)
