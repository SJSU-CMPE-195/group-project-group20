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

//text fields for numerical inputs later (age, weight, etc) -Eric

@Composable
fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }
            onChange(filtered)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

//fitness goal handling, basic three button select for some basic options for now -Eric

@Composable
fun GoalSelector(selected: String, onSelect: (String) -> Unit) {
    val goals = listOf("Lose Weight", "Maintain", "Gain Muscle")

    Column {
        Text("Goal", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { goal ->
                Button(
                    onClick = { onSelect(goal) },
                    colors = if (goal == selected)
                        ButtonDefaults.buttonColors()
                    else
                        ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(goal)
                }
            }
        }
    }
}