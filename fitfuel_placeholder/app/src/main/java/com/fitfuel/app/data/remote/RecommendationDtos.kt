package com.fitfuel.app.data.remote

import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import com.fitfuel.app.model.RecommendationResult
import com.fitfuel.app.model.RecommendedMeal
import com.fitfuel.app.model.TagPreference

// 195B Weeks 3-4: Network request sent only to the FitFuel backend; the OpenAI key never enters the app.
data class RecommendationRequestDto(
    val date: String,
    val goal: String?,
    val remainingCalories: Int?,
    val remainingProtein: Int?,
    val hasUserData: Boolean,
    val candidates: List<MealCandidateDto>,
    // 195B Weeks 5-6: Persistent tag affinities let backend and GPT personalize rankings without storing them server-side yet.
    val tagPreferences: List<TagPreferenceDto> = emptyList()
)

// 195B Weeks 3-4: Candidate meals are supplied by the Android catalog so backend and app stay synchronized.
data class MealCandidateDto(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val tags: List<String>,
    val emoji: String,
    val description: String
)

// 195B Weeks 5-6: Compact preference payload carries affinity and feedback counts for each learned tag.
data class TagPreferenceDto(
    val tag: String,
    val affinityScore: Int,
    val positiveSignals: Int,
    val negativeSignals: Int
)

// 195B Weeks 3-4: Backend response contract for structured GPT or fallback recommendations.
data class RecommendationResponseDto(
    val recommendations: List<RecommendedMealDto>,
    val source: String,
    val message: String? = null
)

// 195B Weeks 3-4: Fully rendered meal data is returned so the UI does not trust model-created nutrition values.
data class RecommendedMealDto(
    val id: String,
    val name: String,
    val calories: Int,
    val protein: Int,
    val tags: List<String>,
    val emoji: String,
    val description: String,
    val reason: String
)

// 195B Weeks 3-4: Converts current day state and the local meal catalog into the backend request contract.
fun buildRecommendationRequest(
    date: String,
    dayEntry: DayEntry?,
    meals: List<Meal>,
    // 195B Weeks 5-6: Request builder includes only persisted preference state, keeping preference learning deterministic.
    preferences: Map<String, TagPreference> = emptyMap()
): RecommendationRequestDto {
    val hasUserData = dayEntry?.calorieTarget != null && dayEntry.proteinTarget != null
    val remainingCalories = dayEntry?.remainingCalories
        ?: dayEntry?.calorieTarget?.minus(dayEntry.caloriesEaten.toIntOrNull() ?: 0)
    val remainingProtein = dayEntry?.remainingProtein
        ?: dayEntry?.proteinTarget?.minus(dayEntry.proteinEaten.toIntOrNull() ?: 0)

    return RecommendationRequestDto(
        date = date,
        goal = dayEntry?.goal,
        remainingCalories = remainingCalories,
        remainingProtein = remainingProtein,
        hasUserData = hasUserData,
        candidates = meals.map { meal ->
            MealCandidateDto(
                id = meal.id,
                name = meal.name,
                calories = meal.calories,
                protein = meal.protein,
                tags = meal.tags,
                emoji = meal.emoji,
                description = meal.description
            )
        },
        // 195B Weeks 5-6: Stable ordering keeps network payloads predictable during debugging and tests.
        tagPreferences = preferences.values
            .sortedBy { it.tag }
            .map { preference ->
                TagPreferenceDto(
                    tag = preference.tag,
                    affinityScore = preference.affinityScore,
                    positiveSignals = preference.positiveSignals,
                    negativeSignals = preference.negativeSignals
                )
            }
    )
}

// 195B Weeks 3-4: Maps validated backend data into the app's recommendation domain model.
fun RecommendationResponseDto.toDomain(): RecommendationResult = RecommendationResult(
    recommendations = recommendations.map { item ->
        RecommendedMeal(
            meal = Meal(
                id = item.id,
                name = item.name,
                calories = item.calories,
                protein = item.protein,
                tags = item.tags,
                emoji = item.emoji,
                description = item.description
            ),
            reason = item.reason
        )
    },
    source = source,
    message = message
)
