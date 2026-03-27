package com.fitfuel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var sex by remember { mutableStateOf("Male") }

    val calorieTarget = calculateCalories(
        weight = weight.toIntOrNull(),
        height = height.toIntOrNull(),
        age = age.toIntOrNull(),
        burned = caloriesBurned.toIntOrNull(),
        goal = goal,
        sex = sex
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "FitFuel",
            style = MaterialTheme.typography.headlineMedium
        )

        NumberField("Weight (kg)", weight) { weight = it }
        NumberField("Height (cm)", height) { height = it }
        NumberField("Age", age) { age = it }
        NumberField("Calories Burned Today", caloriesBurned) { caloriesBurned = it }

        SexSelector(selected = sex, onSelect = { sex = it })
        GoalSelector(selected = goal, onSelect = { goal = it })

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Daily Calorie Target:",
            style = MaterialTheme.typography.titleMedium
        )

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

//user selects their sex here, this will change BMR calculations -Eric

@Composable
fun SexSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("Male", "Female")

    Column {
        Text(
            text = "Sex",
            style = MaterialTheme.typography.titleMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    colors = if (option == selected) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(option)
                }
            }
        }
    }
}

//fitness goal handling, basic three button select for some basic options for now -Eric

@Composable
fun GoalSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    val goals = listOf("Lose Weight", "Maintain", "Gain Muscle")

    Column {
        Text(
            text = "Goal",
            style = MaterialTheme.typography.titleMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { goal ->
                Button(
                    onClick = { onSelect(goal) },
                    colors = if (goal == selected) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(goal)
                }
            }
        }
    }
}

//added field for "if female" case and calculation -Eric

fun calculateCalories(
    weight: Int?,
    height: Int?,
    age: Int?,
    burned: Int?,
    goal: String,
    sex: String
): Int? {
    if (weight == null || height == null || age == null) return null

    val bmr = when (sex) {
        "Female" -> (
            (10 * weight) +
            (6.25 * height) -
            (5 * age) - 161
        )
        else -> (
            (10 * weight) +
            (6.25 * height) -
            (5 * age) + 5
        )
    }.toInt()

    val adjustment = when (goal) {
        "Lose Weight" -> -400
        "Gain Muscle" -> 300
        else -> 0
    }

    return bmr + (burned ?: 0) + adjustment
}
