package com.fitfuel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions //(keep unused for now -Eric)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScreen()
                }
            }
        }
    }
}

@Composable
fun AppScreen() {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var caloriesBurned by remember { mutableStateOf("") }

    var goal by remember { mutableStateOf("Maintain") }

    val calorieTarget = calculateCalories(
        weight.toIntOrNull(),
        height.toIntOrNull(),
        age.toIntOrNull(),
        caloriesBurned.toIntOrNull(),
        goal
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("FitFuel", style = MaterialTheme.typography.headlineMedium)

        NumberField("Weight (kg)", weight) { weight = it }
        NumberField("Height (cm)", height) { height = it }
        NumberField("Age", age) { age = it }
        NumberField("Calories Burned Today", caloriesBurned) { caloriesBurned = it }

        GoalSelector(goal) { goal = it }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Daily Calorie Target:", style = MaterialTheme.typography.titleMedium)
        Text(
            text = calorieTarget?.toString() ?: "Enter all values",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
