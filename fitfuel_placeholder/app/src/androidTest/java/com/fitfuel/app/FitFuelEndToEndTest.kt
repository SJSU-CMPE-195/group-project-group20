package com.fitfuel.app

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fitfuel.app.data.local.FitFuelDatabase
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// 195B Weeks 7-8 (v0.5.0): End-to-end Compose test verifies UI input -> Room -> User stats -> Activity recreation persistence.
@RunWith(AndroidJUnit4::class)
class FitFuelEndToEndTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val database: FitFuelDatabase
        get() = FitFuelDatabase.getInstance(ApplicationProvider.getApplicationContext())

    @Before
    fun clearDatabase() {
        database.clearAllTables()
        composeRule.waitForIdle()
    }

    @After
    fun cleanUpDatabase() {
        database.clearAllTables()
    }

    // 195B Weeks 7-8 (v0.5.0): Navigation regression test protects the four-button layout on phone-sized screens.
    @Test
    fun allPrimaryNavigationButtonsAreVisible() {
        composeRule.onNodeWithTag("nav_daily").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_calendar").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_recommend").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_user").assertIsDisplayed()
    }

    @Test
    fun savedDailyValuesPersistAcrossActivityRecreation() {
        composeRule.onNodeWithTag("input_weight").performTextInput("80")
        composeRule.onNodeWithTag("input_height").performTextInput("180")
        composeRule.onNodeWithTag("input_age").performTextInput("25")
        composeRule.onNodeWithTag("input_calories_burned").performTextInput("300")
        composeRule.onNodeWithTag("input_calories_eaten").performTextInput("1200")
        composeRule.onNodeWithTag("input_protein_eaten").performTextInput("90")
        composeRule.onNodeWithTag("save_day").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            database.dayEntryDao().let { dao ->
                kotlinx.coroutines.runBlocking { dao.getByDate(java.time.LocalDate.now().toString()) != null }
            }
        }

        composeRule.onNodeWithTag("nav_user").performClick()
        composeRule.onNodeWithText("Cumulative Calories Eaten: 1200").assertExists()
        composeRule.onNodeWithText("Cumulative Protein Eaten (g): 90").assertExists()

        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_user").performClick()
        composeRule.onNodeWithText("Cumulative Calories Eaten: 1200").assertExists()
        composeRule.onNodeWithText("Cumulative Protein Eaten (g): 90").assertExists()
    }
}
