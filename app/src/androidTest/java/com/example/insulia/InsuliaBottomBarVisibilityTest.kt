package com.example.insulia

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class InsuliaBottomBarVisibilityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomBar_isVisibleOnHome() {
        composeTestRule.openHome()
        composeTestRule.onNodeWithContentDescription(TabLabel.HOME, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TabLabel.ALERTS, useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun bottomBar_isHiddenOnWelcome() {
        composeTestRule.onNodeWithText(ScreenText.WELCOME).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TabLabel.HOME, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(TabLabel.ALERTS, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE, useUnmergedTree = true).assertDoesNotExist()
    }
}
