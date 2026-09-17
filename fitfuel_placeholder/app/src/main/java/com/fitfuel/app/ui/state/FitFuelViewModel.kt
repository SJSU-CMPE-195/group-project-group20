package com.fitfuel.app.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitfuel.app.data.repository.DayEntryRepository
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

// 195B Weeks 1-2: ViewModel owns navigation state and coordinates repository writes.
class FitFuelViewModel(
    private val repository: DayEntryRepository
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val displayedMonth = MutableStateFlow(YearMonth.now())

    val uiState = combine(
        repository.entries,
        selectedDate,
        displayedMonth
    ) { entries, date, month ->
        FitFuelUiState(
            selectedDate = date,
            displayedMonth = month,
            dayEntries = entries
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FitFuelUiState()
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        displayedMonth.value = YearMonth.from(date)
    }

    fun showPreviousMonth() {
        displayedMonth.value = displayedMonth.value.minusMonths(1)
    }

    fun showNextMonth() {
        displayedMonth.value = displayedMonth.value.plusMonths(1)
    }

    fun saveDay(entry: DayEntry) {
        val date = selectedDate.value
        viewModelScope.launch {
            repository.save(date, entry)
        }
    }

    fun saveMealAsEaten(meal: Meal) {
        val date = selectedDate.value
        viewModelScope.launch {
            repository.addMeal(date, meal)
        }
    }

    // 195B Weeks 1-2: Manual factory avoids adding a dependency-injection framework.
    class Factory(
        private val repository: DayEntryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FitFuelViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return FitFuelViewModel(repository) as T
        }
    }
}
