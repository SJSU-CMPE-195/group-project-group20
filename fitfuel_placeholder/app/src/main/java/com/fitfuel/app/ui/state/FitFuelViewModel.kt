package com.fitfuel.app.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fitfuel.app.data.repository.DayEntryRepository
import com.fitfuel.app.data.repository.RecommendationRepository
import com.fitfuel.app.data.repository.TagPreferenceRepository
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
    private val repository: DayEntryRepository,
    // 195B Weeks 3-4: Recommendation requests are delegated through a backend-aware repository.
    private val recommendationRepository: RecommendationRepository,
    // 195B Weeks 5-6: Preference learning is isolated behind its own persistence repository.
    private val tagPreferenceRepository: TagPreferenceRepository
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val displayedMonth = MutableStateFlow(YearMonth.now())
    // 195B Weeks 3-4: Separate flow prevents network loading from blocking Room updates.
    private val recommendationState = MutableStateFlow(RecommendationUiState())

    // 195B Weeks 7-8 (v0.5.0): Request keys prevent identical automatic recommendation calls after harmless recompositions.
    private var lastRecommendationRequestKey: RecommendationRequestKey? = null

    val uiState = combine(
        repository.entries,
        selectedDate,
        displayedMonth,
        recommendationState,
        // 195B Weeks 5-6: Room preference updates recompose the User screen and future recommendation context.
        tagPreferenceRepository.preferences
    ) { entries, date, month, recommendations, preferences ->
        FitFuelUiState(
            selectedDate = date,
            displayedMonth = month,
            dayEntries = entries,
            // 195B Weeks 5-6: Preference state is part of the single immutable UI state snapshot.
            tagPreferences = preferences,
            recommendationState = recommendations
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FitFuelUiState()
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        displayedMonth.value = YearMonth.from(date)
        // 195B Weeks 3-4: A different day requires a fresh recommendation context.
        recommendationState.value = RecommendationUiState()
        // 195B Weeks 7-8 (v0.5.0): Date changes invalidate the cached recommendation request signature.
        lastRecommendationRequestKey = null
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
            // 195B Weeks 5-6: Accepted recommendations strengthen each associated tag before the day totals refresh.
            tagPreferenceRepository.recordSavedMeal(meal)
            repository.addMeal(date, meal)
        }
    }

    // 195B Weeks 5-6: Explicit rejection records negative tag signals and immediately re-ranks recommendations.
    fun dismissMealRecommendation(meal: Meal) {
        val date = selectedDate.value
        viewModelScope.launch {
            tagPreferenceRepository.recordDismissedMeal(meal)
            val preferences = tagPreferenceRepository.currentPreferences()
            val dayEntry = uiState.value.dayEntries[date]
            val requestKey = buildRecommendationRequestKey(date, dayEntry, preferences)
            refreshRecommendations(date, dayEntry, preferences, requestKey)
        }
    }

    // 195B Weeks 3-4: Loads GPT-ranked meals from FastAPI and exposes fallback status to the UI.
    fun loadRecommendations(dayEntry: DayEntry?, forceRefresh: Boolean = false) {
        if (recommendationState.value.isLoading) return

        val date = selectedDate.value
        viewModelScope.launch {
            // 195B Weeks 5-6: Each request uses the latest persisted tag affinities instead of stale Compose-only state.
            val preferences = tagPreferenceRepository.currentPreferences()
            // 195B Weeks 7-8 (v0.5.0): Automatic loads reuse an unchanged result; explicit Refresh always reaches the backend/fallback pipeline.
            val requestKey = buildRecommendationRequestKey(date, dayEntry, preferences)
            if (!forceRefresh && requestKey == lastRecommendationRequestKey && recommendationState.value.recommendations.isNotEmpty()) {
                return@launch
            }
            refreshRecommendations(date, dayEntry, preferences, requestKey)
        }
    }

    // 195B Weeks 5-6: Shared refresh path keeps normal loads and feedback-triggered personalization consistent.
    private suspend fun refreshRecommendations(
        date: LocalDate,
        dayEntry: DayEntry?,
        preferences: Map<String, com.fitfuel.app.model.TagPreference>,
        // 195B Weeks 7-8 (v0.5.0): Successful results record the exact nutrition/preference context used for cache reuse.
        requestKey: RecommendationRequestKey
    ) {
        recommendationState.value = recommendationState.value.copy(
            isLoading = true,
            message = null
        )

        val result = recommendationRepository.getRecommendations(
            date = date,
            dayEntry = dayEntry,
            preferences = preferences
        )
        recommendationState.value = RecommendationUiState(
            recommendations = result.recommendations,
            isLoading = false,
            source = result.source,
            message = result.message
        )
        // 195B Weeks 7-8 (v0.5.0): Cache is updated only after a complete repository result is available.
        lastRecommendationRequestKey = requestKey
    }

    // 195B Weeks 7-8 (v0.5.0): Compact immutable signature detects meaningful nutrition or preference changes without retaining mutable maps.
    private fun buildRecommendationRequestKey(
        date: LocalDate,
        dayEntry: DayEntry?,
        preferences: Map<String, com.fitfuel.app.model.TagPreference>
    ): RecommendationRequestKey = RecommendationRequestKey(
        date = date,
        dayEntry = dayEntry,
        preferenceSignature = preferences.values
            .sortedBy { it.tag }
            .map { "${it.tag}:${it.affinityScore}:${it.positiveSignals}:${it.negativeSignals}" }
    )

    // 195B Weeks 7-8 (v0.5.0): Private cache model keeps network optimization internal to the ViewModel.
    private data class RecommendationRequestKey(
        val date: LocalDate,
        val dayEntry: DayEntry?,
        val preferenceSignature: List<String>
    )

    // 195B Weeks 1-2: Manual factory avoids adding a dependency-injection framework.
    class Factory(
        private val repository: DayEntryRepository,
        // 195B Weeks 3-4: Factory now receives both persistence and recommendation dependencies.
        private val recommendationRepository: RecommendationRepository,
        // 195B Weeks 5-6: Factory also receives persistent preference-learning storage.
        private val tagPreferenceRepository: TagPreferenceRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FitFuelViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return FitFuelViewModel(repository, recommendationRepository, tagPreferenceRepository) as T
        }
    }
}
