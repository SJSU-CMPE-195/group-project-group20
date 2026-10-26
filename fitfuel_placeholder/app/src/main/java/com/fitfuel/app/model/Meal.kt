package com.fitfuel.app.model

import androidx.compose.runtime.Immutable

// 195B Weeks 7-8 (v0.5.0): Immutable domain models let Compose skip avoidable recomposition when references are unchanged.
@Immutable
data class Meal(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val tags: List<String>,
    val emoji: String,
    val description: String
)
