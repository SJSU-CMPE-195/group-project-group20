package com.fitfuel.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitfuel.app.domain.NutritionCalculator
import com.fitfuel.app.model.DayEntry
import com.fitfuel.app.model.TagPreference
import com.fitfuel.app.ui.RecommendationScreen
import com.fitfuel.app.ui.state.FitFuelViewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    // 195B Weeks 1-2: Activity state is now provided by a lifecycle-aware ViewModel.
    private val fitFuelViewModel: FitFuelViewModel by viewModels {
        FitFuelViewModel.Factory(
            (application as FitFuelApplication).dayEntryRepository,
            // 195B Weeks 3-4: ViewModel receives the FastAPI/OpenAI recommendation repository.
            (application as FitFuelApplication).recommendationRepository,
            // 195B Weeks 5-6: ViewModel receives persistent tag-preference storage for personalization.
            (application as FitFuelApplication).tagPreferenceRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // 195B Weeks 1-2: Room-backed state is collected safely with the Activity lifecycle.
                    val uiState by fitFuelViewModel.uiState.collectAsStateWithLifecycle()
                    val today = remember { LocalDate.now() }
                    val pagerState = rememberPagerState(pageCount = { 4 })
                    val scope = rememberCoroutineScope()

                    Column(modifier = Modifier.fillMaxSize()) {
                        TopNavigationBar(
                            currentPage = pagerState.currentPage,
                            onNavigate = { page ->
                                scope.launch { pagerState.animateScrollToPage(page) }
                            }
                        )

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            when (page) {
                                0 -> DailyCalculationScreen(
                                    selectedDate = uiState.selectedDate,
                                    savedEntry = uiState.dayEntries[uiState.selectedDate],
                                    onSaveEntry = fitFuelViewModel::saveDay
                                )

                                1 -> CalendarScreen(
                                    today = today,
                                    selectedDate = uiState.selectedDate,
                                    displayedMonth = uiState.displayedMonth,
                                    entries = uiState.dayEntries,
                                    onPreviousMonth = fitFuelViewModel::showPreviousMonth,
                                    onNextMonth = fitFuelViewModel::showNextMonth,
                                    onSelectDate = { date ->
                                        fitFuelViewModel.selectDate(date)
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    }
                                )

                                2 -> RecommendationScreen(
                                    selectedDateLabel = uiState.selectedDate.toString(),
                                    currentEntry = uiState.dayEntries[uiState.selectedDate],
                                    // 195B Weeks 3-4: GPT recommendation state and refresh action are owned by the ViewModel.
                                    recommendationState = uiState.recommendationState,
                                    // 195B Weeks 5-6: Recommendation UI receives learned tags and explicit feedback actions.
                                    tagPreferences = uiState.tagPreferences,
                                    onRequestRecommendations = {
                                        fitFuelViewModel.loadRecommendations(
                                            uiState.dayEntries[uiState.selectedDate]
                                        )
                                    },
                                    // 195B Weeks 7-8 (v0.5.0): Manual refresh bypasses the recommendation cache while automatic recompositions reuse unchanged results.
                                    onRefreshRecommendations = {
                                        fitFuelViewModel.loadRecommendations(
                                            uiState.dayEntries[uiState.selectedDate],
                                            forceRefresh = true
                                        )
                                    },
                                    onSaveMealAsEaten = fitFuelViewModel::saveMealAsEaten,
                                    onDismissRecommendation = fitFuelViewModel::dismissMealRecommendation
                                )

                                3 -> UserStatsScreen(
                                    today = today,
                                    entries = uiState.dayEntries,
                                    // 195B Weeks 5-6: User page now displays learned preference tags from Room.
                                    tagPreferences = uiState.tagPreferences
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

//Edited this to do button wrap-around instead of displaying text vertically. -Eric

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopNavigationBar(
    currentPage: Int,
    onNavigate: (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2
    ) {
        NavButton("Daily", currentPage == 0) { onNavigate(0) }
        NavButton("Calendar", currentPage == 1) { onNavigate(1) }
        NavButton("Recommend", currentPage == 2) { onNavigate(2) }
        NavButton("User", currentPage == 3) { onNavigate(3) }
    }
}

@Composable
fun NavButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        // 195B Weeks 7-8 (v0.5.0): Stable semantics tags make navigation deterministic in Compose end-to-end tests.
        modifier = Modifier.testTag("nav_${text.lowercase()}") ,
        colors = if (selected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.outlinedButtonColors()
        }
    ) {
        Text(text)
    }
}

@Composable
fun DailyCalculationScreen(
    selectedDate: LocalDate,
    savedEntry: DayEntry?,
    onSaveEntry: (DayEntry) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var caloriesBurned by remember { mutableStateOf("") }
    var caloriesEaten by remember { mutableStateOf("") }
    var proteinEaten by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("Maintain") }
    var sex by remember { mutableStateOf("Male") }

    LaunchedEffect(selectedDate, savedEntry) {
        weight = savedEntry?.weight ?: ""
        height = savedEntry?.height ?: ""
        age = savedEntry?.age ?: ""
        caloriesBurned = savedEntry?.caloriesBurned ?: ""
        caloriesEaten = savedEntry?.caloriesEaten ?: ""
        proteinEaten = savedEntry?.proteinEaten ?: ""
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

    val proteinTarget = calculateProtein(weight = weight.toIntOrNull(), goal = goal)
    val remainingCalories = calorieTarget?.minus(caloriesEaten.toIntOrNull() ?: 0)
    val remainingProtein = proteinTarget?.minus(proteinEaten.toIntOrNull() ?: 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "FitFuel", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Selected Day: $selectedDate", style = MaterialTheme.typography.titleMedium)

        // 195B Weeks 7-8 (v0.5.0): Input semantics tags support repeatable UI automation without depending on visible label parsing.
        NumberField("Weight (kg)", weight, "input_weight") { weight = it }
        NumberField("Height (cm)", height, "input_height") { height = it }
        NumberField("Age", age, "input_age") { age = it }
        NumberField("Calories Burned Today", caloriesBurned, "input_calories_burned") { caloriesBurned = it }
        NumberField("Calories Eaten Today", caloriesEaten, "input_calories_eaten") { caloriesEaten = it }
        NumberField("Protein Eaten Today (g)", proteinEaten, "input_protein_eaten") { proteinEaten = it }

        SexSelector(selected = sex, onSelect = { sex = it })
        GoalSelector(selected = goal, onSelect = { goal = it })

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Daily Calorie Target", style = MaterialTheme.typography.titleMedium)
                Text(text = calorieTarget?.toString() ?: "Enter all values", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Remaining Calories", style = MaterialTheme.typography.titleMedium)
                Text(text = remainingCalories?.toString() ?: "Enter calories eaten", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Daily Protein Target (g)", style = MaterialTheme.typography.titleMedium)
                Text(text = proteinTarget?.toString() ?: "Enter weight", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Remaining Protein (g)", style = MaterialTheme.typography.titleMedium)
                Text(text = remainingProtein?.toString() ?: "Enter protein eaten", style = MaterialTheme.typography.headlineSmall)
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
                        proteinEaten = proteinEaten,
                        sex = sex,
                        goal = goal,
                        calorieTarget = calorieTarget,
                        remainingCalories = remainingCalories,
                        proteinTarget = proteinTarget,
                        remainingProtein = remainingProtein
                    )
                )
            },
            // 195B Weeks 7-8 (v0.5.0): Save action has a stable test hook for the persistence end-to-end scenario.
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_day")
        ) {
            Text("Save Day")
        }
    }
}

//Extremely basic calendar screen implementation. Stores today's date, user selected day, and days where info is recorded. -Eric

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
    // 195B Weeks 7-8 (v0.5.0): Calendar cells are reused until the displayed month changes, reducing repeated date allocation during recomposition.
    val days = remember(displayedMonth) { buildCalendarCells(displayedMonth) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Calendar", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onPreviousMonth) { Text("< Prev") }
            Text(
                text = "${displayedMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedMonth.year}",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onNextMonth) { Text("Next >") }
        }

        Spacer(modifier = Modifier.height(8.dp))
        WeekHeader()
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { date ->
                if (date == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    CalendarDayCell(
                        date = date,
                        isToday = date == today,
                        isSelected = date == selectedDate,
                        hasSavedEntry = entries.containsKey(date),
                        onClick = { onSelectDate(date) }
                    )
                }
            }
        }
    }
}

//handles the User screen -Zihao

@Composable
fun UserStatsScreen(
    today: LocalDate,
    entries: Map<LocalDate, DayEntry>,
    // 195B Weeks 5-6: User statistics include the preference profile learned from recommendation feedback.
    tagPreferences: Map<String, TagPreference>
) {
    // 195B Weeks 7-8 (v0.5.0): Aggregate statistics are memoized so unrelated UI state changes do not repeatedly rescan every saved day.
    val savedDays = remember(entries) { entries.keys.sorted() }
    val firstDate = remember(savedDays) { savedDays.firstOrNull() }
    val daysElapsed = remember(firstDate, today) {
        if (firstDate != null) ChronoUnit.DAYS.between(firstDate, today).toInt() else 0
    }
    val totalCaloriesBurned = remember(entries) { entries.values.sumOf { it.caloriesBurned.toIntOrNull() ?: 0 } }
    val totalCaloriesEaten = remember(entries) { entries.values.sumOf { it.caloriesEaten.toIntOrNull() ?: 0 } }
    val totalProteinTarget = remember(entries) { entries.values.sumOf { it.proteinTarget ?: 0 } }
    val totalProteinEaten = remember(entries) { entries.values.sumOf { it.proteinEaten.toIntOrNull() ?: 0 } }
    // 195B Weeks 5-6: Only positive affinity tags are shown as preferred attributes; negative signals still affect ranking.
    val preferredTags = remember(tagPreferences) {
        tagPreferences.values
            .filter { it.affinityScore > 0 }
            .sortedWith(
                compareByDescending<TagPreference> { it.affinityScore }
                    .thenByDescending { it.positiveSignals }
                    .thenBy { it.tag }
            )
            .take(8)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 195B Weeks 7-8 (v0.5.0): Screen-level semantics tag supports end-to-end navigation assertions.
            .testTag("user_stats_screen")
            // 195B Weeks 5-6: Preference rows can extend the User page, so the screen now scrolls on smaller devices.
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "User Stats", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Days Since Start: $daysElapsed", style = MaterialTheme.typography.titleMedium)
                Text("Journey Start Date: ${firstDate ?: "No data yet"}")
                Text("Saved Days: ${entries.size}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cumulative Calories Burned: $totalCaloriesBurned", style = MaterialTheme.typography.titleMedium)
                Text("Cumulative Calories Eaten: $totalCaloriesEaten", style = MaterialTheme.typography.titleMedium)
                Text("Cumulative Protein Target (g): $totalProteinTarget", style = MaterialTheme.typography.titleMedium)
                Text("Cumulative Protein Eaten (g): $totalProteinEaten", style = MaterialTheme.typography.titleMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Preferred Meal Tags", style = MaterialTheme.typography.titleMedium)
                if (preferredTags.isEmpty()) {
                    // 195B Weeks 5-6: Empty state explains how the preference profile begins learning.
                    Text("No preferred tags learned yet. Save recommended meals to build preferences.")
                } else {
                    // 195B Weeks 5-6: Scores and signal counts make the proof-of-concept learning behavior observable.
                    preferredTags.forEach { preference ->
                        Text(
                            text = "#${preference.tag}  •  score ${preference.affinityScore}  •  ${preference.positiveSignals} positive signal(s)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
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
            Text(text = date.dayOfMonth.toString(), color = textColor, style = MaterialTheme.typography.bodyLarge)

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

//text fields for numerical inputs (age, weight, etc) -Eric

@Composable
fun NumberField(
    label: String,
    value: String,
    // 195B Weeks 7-8 (v0.5.0): Each numeric field exposes a stable ID for Compose UI tests.
    testTag: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() }) },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
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
        Text(text = "Sex", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Button(
                    onClick = { onSelect(option) },
                    colors = if (option == selected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
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
        Text(text = "Goal", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { goal ->
                Button(
                    onClick = { onSelect(goal) },
                    colors = if (goal == selected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(goal)
                }
            }
        }
    }
}

//Basic caloric and protein goal calculation with sex selector. Using Mifflin-St Jeor equation. -Eric

// 195B Weeks 7-8 (v0.5.0): Existing call sites delegate to a pure domain calculator that is covered by JVM unit tests.
fun calculateCalories(
    weight: Int?,
    height: Int?,
    age: Int?,
    burned: Int?,
    goal: String,
    sex: String
): Int? = NutritionCalculator.calculateCalories(weight, height, age, burned, goal, sex)

fun calculateProtein(
    weight: Int?,
    goal: String
): Int? = NutritionCalculator.calculateProtein(weight, goal)

//handles the calendar cells on the calendar screen. -Zihao

fun buildCalendarCells(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val startOffset = firstDay.dayOfWeek.value % 7

    val cells = mutableListOf<LocalDate?>()
    repeat(startOffset) { cells.add(null) }
    for (day in 1..daysInMonth) {
        cells.add(month.atDay(day))
    }
    return cells
}
