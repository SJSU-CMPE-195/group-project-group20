package com.fitfuel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

data class DayEntry(
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val caloriesBurned: String = "",
    val caloriesEaten: String = "",
    val proteinEaten: String = "",
    val sex: String = "Male",
    val goal: String = "Maintain",
    val calorieTarget: Int? = null,
    val remainingCalories: Int? = null,
    val proteinTarget: Int? = null,
    val remainingProtein: Int? = null
)

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val today = remember { LocalDate.now() }
                    var selectedDate by remember { mutableStateOf(today) }
                    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }

                    val dayEntries = remember { mutableStateMapOf<LocalDate, DayEntry>() }

                    val pagerState = rememberPagerState(pageCount = { 3 })
                    val scope = rememberCoroutineScope()

                    Column(modifier = Modifier.fillMaxSize()) {
                        TopNavigationBar(
                            currentPage = pagerState.currentPage,
                            onNavigate = { page ->
                                scope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            }
                        )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> DailyCalculationScreen(
                                selectedDate = selectedDate,
                                savedEntry = dayEntries[selectedDate],
                                onSaveEntry = { entry ->
                                    dayEntries[selectedDate] = entry
                                },
                                onGoToCalendar = {
                                    scope.launch { pagerState.animateScrollToPage(1) }
                                    },
                                    onGoToUser = {
                                        scope.launch { pagerState.animateScrollToPage(2) }
                                }
                            )

                            1 -> CalendarScreen(
                                today = today,
                                selectedDate = selectedDate,
                                displayedMonth = displayedMonth,
                                entries = dayEntries,
                                onPreviousMonth = {
                                    displayedMonth = displayedMonth.minusMonths(1)
                                },
                                onNextMonth = {
                                    displayedMonth = displayedMonth.plusMonths(1)
                                },
                                onSelectDate = { date ->
                                    selectedDate = date
                                    displayedMonth = YearMonth.from(date)
                                    scope.launch { pagerState.animateScrollToPage(0) }
                                }
                            )

                                2 -> UserStatsScreen(
                                    today = today,
                                    entries = dayEntries
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyCalculationScreen(
    selectedDate: LocalDate,
    savedEntry: DayEntry?,
    onSaveEntry: (DayEntry) -> Unit,
    onGoToCalendar: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var caloriesBurned by remember { mutableStateOf("") }
    var caloriesEaten by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Maintain") }
    var sex by remember { mutableStateOf("Male") }

    LaunchedEffect(selectedDate, savedEntry) {
        weight = savedEntry?.weight ?: ""
        height = savedEntry?.height ?: ""
        age = savedEntry?.age ?: ""
        caloriesBurned = savedEntry?.caloriesBurned ?: ""
        caloriesEaten = savedEntry?.caloriesEaten ?: ""
        goal = savedEntry?.goal ?: "Maintain"
        sex = savedEntry?.sex ?: "Male"
    }

    val calorieTarget = calculateCalories(
        weight = weight.toIntOrNull(),
        height = height.toIntOrNull(),
        age = age.toIntOrNull(),
        burned = caloriesBurned.toIntOrNull(),
        goal = goal,
        sex = sex
    )

    val remainingCalories = calorieTarget?.minus(caloriesEaten.toIntOrNull() ?: 0)

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

        Text(
            text = "Selected Day: $selectedDate",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedButton(onClick = onGoToCalendar) {
            Text("Open Calendar")
        }

        NumberField("Weight (kg)", weight) { weight = it }
        NumberField("Height (cm)", height) { height = it }
        NumberField("Age", age) { age = it }
        NumberField("Calories Burned Today", caloriesBurned) { caloriesBurned = it }
        NumberField("Calories Eaten Today", caloriesEaten) { caloriesEaten = it }

        SexSelector(selected = sex, onSelect = { sex = it })
        GoalSelector(selected = goal, onSelect = { goal = it })

        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Calorie Target",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = calorieTarget?.toString() ?: "Enter all values",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Remaining Calories",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = remainingCalories?.toString() ?: "Enter calories eaten",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Button(
            onClick = {
                onSaveEntry(
                    DayEntry(
                        weight = weight,
                        height = height,
                        age = age,
                        caloriesBurned = caloriesBurned,
                        caloriesEaten = caloriesEaten,
                        sex = sex,
                        goal = goal,
                        calorieTarget = calorieTarget,
                        remainingCalories = remainingCalories
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Day")
        }
    }
}

//Extremely basic calendar screen implementation. Will use this to store user information in the future. Line 325 is NOT FUNCTIONAL at the given moment. -Eric

@Composable
fun CalendarScreen(
    today: LocalDate,
    selectedDate: LocalDate,
    displayedMonth: YearMonth,
    entries: Map<LocalDate, DayEntry>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val days = buildCalendarCells(displayedMonth)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Calendar",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPreviousMonth) {
                Text("< Prev")
            }

            Text(
                text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedMonth.year}",
                style = MaterialTheme.typography.titleLarge
            )

            TextButton(onClick = onNextMonth) {
                Text("Next >")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        WeekHeader()

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = true
        ) {
            items(days) { date ->
                if (date == null) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                    )
                } else {
                    val isToday = date == today
                    val isSelected = date == selectedDate
                    val hasSavedEntry = entries.containsKey(date)

                    CalendarDayCell(
                        date = date,
                        isToday = isToday,
                        isSelected = isSelected,
                        hasSavedEntry = hasSavedEntry,
                        onClick = { onSelectDate(date) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tap a day to edit that day's calories and save them.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun WeekHeader() {
    val days = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

//Basic calendar screen placeholder UI template. -Eric

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasSavedEntry: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Color(0xFFB71C1C)
        isToday -> Color(0xFFFFCDD2)
        else -> Color(0xFFF5F5F5)
    }

    val textColor = if (isSelected) Color.White else Color.Black
    val borderColor = if (isToday) Color.Red else Color.LightGray

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .border(1.dp, borderColor, MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )

            if (hasSavedEntry) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (isSelected) Color.White else Color.Red,
                            shape = MaterialTheme.shapes.small
                        )
                        .align(Alignment.End)
                )
            }
        }
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
