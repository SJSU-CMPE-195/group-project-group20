package com.fitfuel.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// 195B Weeks 5-6: Room entity stores learned preference signals per normalized meal tag.
@Entity(tableName = "tag_preferences")
data class TagPreferenceEntity(
    @PrimaryKey val tag: String,
    val affinityScore: Int,
    val positiveSignals: Int,
    val negativeSignals: Int,
    val lastUpdatedEpochMillis: Long
)
