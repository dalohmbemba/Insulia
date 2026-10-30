package com.example.insulia

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test

class InsuliaTabSelectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeTab_selectedOnHome() {
        composeTestRule.openHome()
        composeTestRule.onNodeWithContentDescription(TabLabel.HOME, useUnmergedTree = true).assertIsSelected()
        composeTestRule.onNodeWithContentDescription(TabLabel.ALERTS, useUnmergedTree = true).assertIsNotSelected()
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE, useUnmergedTree = true).assertIsNotSelected()
    }

    @Test
    fun profileTab_selectedOnProfile() {
        composeTestRule.openProfile()
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE, useUnmergedTree = true).assertIsSelected()
    }
}
