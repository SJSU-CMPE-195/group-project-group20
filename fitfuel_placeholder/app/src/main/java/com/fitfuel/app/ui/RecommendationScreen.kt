package com.fitfuel.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import com.fitfuel.app.model.RecommendedMeal
import com.fitfuel.app.model.TagPreference
import com.fitfuel.app.ui.state.RecommendationUiState

@Composable
fun RecommendationScreen(
    selectedDateLabel: String,
    currentEntry: DayEntry?,
    // 195B Weeks 3-4: Screen renders asynchronous recommendation state owned by the ViewModel.
    recommendationState: RecommendationUiState,
    // 195B Weeks 5-6: Persisted preferences are shown as personalization context without being edited directly here.
    tagPreferences: Map<String, TagPreference>,
    // 195B Weeks 3-4: Refresh callback triggers the FastAPI/OpenAI recommendation pipeline.
    onRequestRecommendations: () -> Unit,
    // 195B Weeks 7-8 (v0.5.0): Explicit refresh bypasses ViewModel request caching while automatic loads stay deduplicated.
    onRefreshRecommendations: () -> Unit,
    onSaveMealAsEaten: (Meal) -> Unit,
    // 195B Weeks 5-6: Explicit negative feedback teaches the tag system and triggers re-ranking.
    onDismissRecommendation: (Meal) -> Unit
) {
    // 195B Weeks 3-4: Recommendations refresh whenever the selected date or its saved nutrition data changes.
    LaunchedEffect(selectedDateLabel, currentEntry) {
        onRequestRecommendations()
    }

    var selectedMeal by remember { mutableStateOf<RecommendedMeal?>(null) }
    // 195B Weeks 5-6: Count only learned non-zero signals so the UI can explain when personalization is active.
    val learnedPreferenceCount = tagPreferences.values.count { it.affinityScore != 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recommendations",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Selected Day: $selectedDateLabel",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (currentEntry?.calorieTarget == null || currentEntry.proteinTarget == null) {
                "No full day data yet. Showing 3 sample meals."
            } else {
                "Meals are ranked against today's remaining calorie and protein needs."
            },
            style = MaterialTheme.typography.bodyMedium
        )

        // 195B Weeks 5-6: Personalization status makes it clear when past meal feedback is affecting rankings.
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (learnedPreferenceCount == 0) {
                "No learned meal preferences yet. Save meals or choose 'Not for me' to teach FitFuel."
            } else {
                "Personalization active: $learnedPreferenceCount learned meal tag(s)."
            },
            style = MaterialTheme.typography.bodySmall
        )

        // 195B Weeks 3-4: Source status makes GPT, backend fallback, and Android fallback visible during development.
        recommendationState.source?.let { source ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recommendation source: ${source.replace('_', ' ')}",
                style = MaterialTheme.typography.labelMedium
            )
        }

        recommendationState.message?.let { message ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // 195B Weeks 7-8 (v0.5.0): Keep existing cards visible during refresh and show lightweight progress instead of blanking the screen.
        if (recommendationState.isLoading && recommendationState.recommendations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recommendation_refresh_progress")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 195B Weeks 3-4: Loading UI prevents an empty page while the backend waits for OpenAI.
        if (recommendationState.isLoading && recommendationState.recommendations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(
                    items = recommendationState.recommendations,
                    key = { it.meal.id },
                    // 195B Weeks 7-8 (v0.5.0): Stable content type improves LazyColumn item reuse during ranking refreshes.
                    contentType = { "meal_recommendation" }
                ) { recommendation ->
                    MealCard(
                        recommendedMeal = recommendation,
                        onClick = { selectedMeal = recommendation }
                    )
                }

                // 195B Weeks 3-4: Manual retry supports backend startup and temporary network failures.
                item {
                    OutlinedButton(
                        onClick = onRefreshRecommendations,
                        enabled = !recommendationState.isLoading,
                        // 195B Weeks 7-8 (v0.5.0): Stable refresh tag supports UI tests and accessibility inspection.
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("refresh_recommendations")
                    ) {
                        Text(if (recommendationState.isLoading) "Loading..." else "Refresh Recommendations")
                    }
                }
            }
        }
    }

    selectedMeal?.let { recommendation ->
        MealDetailDialog(
            recommendedMeal = recommendation,
            onDismiss = { selectedMeal = null },
            onSave = {
                onSaveMealAsEaten(recommendation.meal)
                selectedMeal = null
            },
            // 195B Weeks 5-6: Rejection closes the dialog while the ViewModel records feedback and refreshes results.
            onReject = {
                onDismissRecommendation(recommendation.meal)
                selectedMeal = null
            }
        )
    }
}

@Composable
private fun MealCard(
    recommendedMeal: RecommendedMeal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // 195B Weeks 7-8 (v0.5.0): Meal IDs become stable card semantics for future recommendation UI tests.
            .testTag("meal_card_${recommendedMeal.meal.id}")
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFFFE5E5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = recommendedMeal.meal.emoji,
                    style = MaterialTheme.typography.displayMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = recommendedMeal.meal.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = recommendedMeal.meal.description,
                style = MaterialTheme.typography.bodyMedium
            )

            // 195B Weeks 5-6: Visible tags help users understand which meal attributes drive future personalization.
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = recommendedMeal.meal.tags.joinToString(" • "),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun MealDetailDialog(
    recommendedMeal: RecommendedMeal,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    // 195B Weeks 5-6: Detail dialog exposes explicit negative feedback instead of inferring preferences from inactivity.
    onReject: () -> Unit
) {
    val meal = recommendedMeal.meal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = meal.name)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFFFFE5E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = meal.emoji,
                        style = MaterialTheme.typography.displayLarge
                    )
                }

                Text(text = "Calories: ${meal.calories}")
                Text(text = "Protein: ${meal.protein} g")
                Text(text = "Tags: ${meal.tags.joinToString()}")
                Text(text = recommendedMeal.reason)
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Save as eaten")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // 195B Weeks 5-6: "Not for me" records a durable negative tag signal used by future rankings.
                TextButton(onClick = onReject) {
                    Text("Not for me")
                }
                OutlinedButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
