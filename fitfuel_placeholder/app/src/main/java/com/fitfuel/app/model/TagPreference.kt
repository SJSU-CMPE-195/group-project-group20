package com.fitfuel.app.model

import androidx.compose.runtime.Immutable

// 195B Weeks 5-6: Persistent tag affinity summarizes explicit meal feedback for personalization.
// 195B Weeks 7-8 (v0.5.0): Immutable domain models let Compose skip avoidable recomposition when references are unchanged.
@Immutable
data class TagPreference(
    val tag: String,
    val affinityScore: Int,
    val positiveSignals: Int,
    val negativeSignals: Int,
    val lastUpdatedEpochMillis: Long
)
