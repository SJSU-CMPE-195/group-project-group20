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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.Meal
import com.fitfuel.app.recommendation.RecommendationEngine

@Composable
fun RecommendationScreen(
    selectedDateLabel: String,
    currentEntry: DayEntry?,
    onSaveMealAsEaten: (Meal) -> Unit
) {
    val recommendations = remember(currentEntry) {
        RecommendationEngine.getRecommendations(currentEntry)
    }

    var selectedMeal by remember { mutableStateOf<RecommendationEngine.RecommendedMeal?>(null) }

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
                "No full day data yet. Showing 3 demo meals."
            } else {
                "These 3 meals are ranked against today's current calorie and protein needs."
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(recommendations) { recommendation ->
                MealCard(
                    recommendedMeal = recommendation,
                    onClick = { selectedMeal = recommendation }
                )
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
            }
        )
    }
}

@Composable
private fun MealCard(
    recommendedMeal: RecommendationEngine.RecommendedMeal,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
        }
    }
}

@Composable
private fun MealDetailDialog(
    recommendedMeal: RecommendationEngine.RecommendedMeal,
    onDismiss: () -> Unit,
    onSave: () -> Unit
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
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
