package com.example.insulia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class InsuliaNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun alertsTab_opensAlerts() {
        composeTestRule.openHome()
        composeTestRule.selectTab(TabLabel.ALERTS)
        composeTestRule.onNodeWithText(ScreenText.ALERTS).assertIsDisplayed()
    }

    @Test
    fun profileTab_opensProfile() {
        composeTestRule.openHome()
        composeTestRule.selectTab(TabLabel.PROFILE)
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()
    }

    @Test
    fun homeTab_returnsToHome() {
        composeTestRule.openHome()
        composeTestRule.selectTab(TabLabel.ALERTS)
        composeTestRule.selectTab(TabLabel.HOME)
        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun backAction_returnsToHome() {
        composeTestRule.openProfile()
        composeTestRule.goBack()
        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun appointmentsQuickAction_opensAppointments() {
        composeTestRule.openHome()
        composeTestRule.onNodeWithText("Appointments").performClick()
        composeTestRule.onNodeWithText(ScreenText.APPOINTMENTS).assertIsDisplayed()
    }
}
