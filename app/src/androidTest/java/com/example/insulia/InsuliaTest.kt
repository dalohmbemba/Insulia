package com.example.insulia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

/**
 * Smoke tests for the app shell.
 *
 * The full behaviour is covered by [InsuliaNavigationTest],
 * [InsuliaBottomBarTest] and [InsuliaButtonsTest]; this suite only proves that
 * the app launches with the demo content and reaches the dashboard.
 */
class InsuliaTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunches_showingTheWelcomeScreen() {
        composeTestRule.onNodeWithText(ScreenText.WELCOME).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Insulia logo").assertExists()
    }

    @Test
    fun getStarted_opensTheDashboard() {
        composeTestRule.openHome()

        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
        composeTestRule.onNodeWithText(ScreenText.WELCOME).assertDoesNotExist()
    }

    @Test
    fun dashboard_showsTheSeededDemoReading() {
        composeTestRule.openHome()

        composeTestRule.onNodeWithText("145 mg/dL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Glucose Trend").assertExists()
        composeTestRule.onNodeWithText("Diet Suggestion").assertExists()
    }

    @Test
    fun bottomBar_isReachableAfterGetStarted() {
        composeTestRule.openHome()

        composeTestRule.onNodeWithContentDescription(TabLabel.ALERTS)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText(ScreenText.ALERTS).assertIsDisplayed()
    }
}
