package com.fitfuel.app.ui.state

import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.TagPreference
import java.time.LocalDate
import java.time.YearMonth

// 195B Weeks 1-2: Immutable app-level state observed by the Compose UI.
data class FitFuelUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.now(),
    val dayEntries: Map<LocalDate, DayEntry> = emptyMap(),
    // 195B Weeks 5-6: Learned tag affinities are exposed to the User and Recommendation screens.
    val tagPreferences: Map<String, TagPreference> = emptyMap(),
    // 195B Weeks 3-4: Remote recommendation state is exposed alongside Room-backed day state.
    val recommendationState: RecommendationUiState = RecommendationUiState()
)
