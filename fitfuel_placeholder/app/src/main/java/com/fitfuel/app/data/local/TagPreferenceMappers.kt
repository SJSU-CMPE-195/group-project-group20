package com.fitfuel.app.data.local

import com.fitfuel.app.model.TagPreference

// 195B Weeks 5-6: Keeps the Room representation separate from the recommendation-domain model.
fun TagPreferenceEntity.toModel(): TagPreference = TagPreference(
    tag = tag,
    affinityScore = affinityScore,
    positiveSignals = positiveSignals,
    negativeSignals = negativeSignals,
    lastUpdatedEpochMillis = lastUpdatedEpochMillis
)
