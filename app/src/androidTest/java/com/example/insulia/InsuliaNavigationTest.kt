package com.example.insulia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import org.junit.Rule
import org.junit.Test

/**
 * Navigation between the screens.
 *
 * These tests follow the journeys a patient can take: the welcome screen, the
 * three tabs, every entry in the profile list and the back arrow in the page
 * header. The back stack is checked explicitly so "back" always returns to the
 * screen the user came from.
 */
class InsuliaNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun getStarted_replacesWelcomeWithHome() {
        composeTestRule.onNodeWithText(ScreenText.WELCOME).performClick()

        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
        composeTestRule.onNodeWithText(ScreenText.WELCOME).assertDoesNotExist()
    }

    @Test
    fun alertsTab_opensNotifications_andBackReturnsHome() {
        composeTestRule.openHome()

        composeTestRule.selectTab(TabLabel.ALERTS)
        composeTestRule.onNodeWithText(ScreenText.ALERTS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Medication reminder").assertExists()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun profileTab_opensProfile_andBackReturnsHome() {
        composeTestRule.openHome()

        composeTestRule.selectTab(TabLabel.PROFILE)
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun profile_opensPersonalRecords_andBackReturnsToProfile() {
        composeTestRule.openProfile()

        composeTestRule.openProfileDestination("Personal Records")
        composeTestRule.onNodeWithText(ScreenText.PERSONAL_RECORDS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Madalo Mbemba").assertExists()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()
    }

    @Test
    fun profile_opensGlucoseLevels_andBackReturnsToProfile() {
        composeTestRule.openProfile()

        composeTestRule.openProfileDestination("Glucose levels")
        composeTestRule.onNodeWithText(ScreenText.GLUCOSE_LEVELS).assertIsDisplayed()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()
    }

    @Test
    fun profile_opensAppointments_andBackReturnsToProfile() {
        composeTestRule.openProfile()

        composeTestRule.openProfileDestination("Appointments")
        composeTestRule.onNodeWithText(ScreenText.APPOINTMENTS).assertIsDisplayed()
        composeTestRule.onNodeWithText("Book now").assertExists()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()
    }

    @Test
    fun profile_opensInstructionSection_andBackReturnsToProfile() {
        composeTestRule.openProfile()

        composeTestRule.openProfileDestination("Instruction Section")
        composeTestRule.onNodeWithText(ScreenText.INSTRUCTIONS).assertIsDisplayed()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()
    }

    @Test
    fun profile_opensEducationSection_andBackReturnsToProfile() {
        composeTestRule.openProfile()

        composeTestRule.openProfileDestination("Access education")
        composeTestRule.onNodeWithText(ScreenText.INSTRUCTIONS).assertIsDisplayed()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()
    }

    @Test
    fun learnQuickAction_opensInstructions_andBackReturnsHome() {
        composeTestRule.openHome()

        composeTestRule.onNodeWithContentDescription("Learn").performClick()
        composeTestRule.onNodeWithText(ScreenText.INSTRUCTIONS).assertIsDisplayed()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun backStack_returnsThroughEveryVisitedScreen() {
        composeTestRule.openProfile()
        composeTestRule.openProfileDestination("Appointments")

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()

        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun systemBack_returnsToPreviousScreen() {
        composeTestRule.openProfile()

        Espresso.pressBack()

        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun systemBack_fromTabScreen_returnsToHome() {
        composeTestRule.openAlerts()

        Espresso.pressBack()

        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun systemBack_fromSubScreen_returnsToProfile() {
        composeTestRule.openAppointments()

        Espresso.pressBack()

        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()
    }
}
