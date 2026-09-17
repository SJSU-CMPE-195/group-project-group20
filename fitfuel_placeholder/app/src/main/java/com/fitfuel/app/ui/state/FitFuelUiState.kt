package com.fitfuel.app.ui.state

import com.fitfuel.app.model.DayEntry
import java.time.LocalDate
import java.time.YearMonth

// 195B Weeks 1-2: Immutable app-level state observed by the Compose UI.
data class FitFuelUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.now(),
    val dayEntries: Map<LocalDate, DayEntry> = emptyMap()
)
