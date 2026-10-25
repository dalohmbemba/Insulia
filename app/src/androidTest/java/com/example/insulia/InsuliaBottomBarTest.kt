package com.example.insulia

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import org.junit.Rule
import org.junit.Test

/**
 * The bottom navigation bar.
 *
 * The bar has to be reachable on every screen that has it, the correct item has
 * to be highlighted, and switching tabs must not grow the back stack.
 */
class InsuliaBottomBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomBar_isHiddenOnWelcomeScreen() {
        composeTestRule.onNodeWithText(ScreenText.WELCOME).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(TabLabel.HOME).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(TabLabel.ALERTS).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE).assertDoesNotExist()
    }

    @Test
    fun bottomBar_itemsAreVisibleAndClickableOnEveryTab() {
        composeTestRule.openHome()
        assertBarIsClickable()

        composeTestRule.selectTab(TabLabel.ALERTS)
        assertBarIsClickable()

        composeTestRule.selectTab(TabLabel.PROFILE)
        assertBarIsClickable()
    }

    @Test
    fun bottomBar_isVisibleOnSubScreens() {
        composeTestRule.openPersonalRecords()
        assertBarIsClickable()

        composeTestRule.goBack()
        composeTestRule.openProfileDestination("Glucose levels")
        assertBarIsClickable()

        composeTestRule.goBack()
        composeTestRule.openProfileDestination("Appointments")
        assertBarIsClickable()

        composeTestRule.goBack()
        composeTestRule.openProfileDestination("Instruction Section")
        assertBarIsClickable()
    }

    @Test
    fun selectedTab_isHighlighted() {
        composeTestRule.openHome()

        composeTestRule.onNodeWithContentDescription(TabLabel.HOME).assertIsSelected()
        composeTestRule.onNodeWithContentDescription(TabLabel.ALERTS).assertIsNotSelected()
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE).assertIsNotSelected()

        composeTestRule.selectTab(TabLabel.ALERTS)

        composeTestRule.onNodeWithContentDescription(TabLabel.ALERTS).assertIsSelected()
        composeTestRule.onNodeWithContentDescription(TabLabel.HOME).assertIsNotSelected()
    }

    @Test
    fun tappingTabs_switchesBetweenScreens() {
        composeTestRule.openHome()

        composeTestRule.selectTab(TabLabel.ALERTS)
        composeTestRule.onNodeWithText(ScreenText.ALERTS).assertIsDisplayed()

        composeTestRule.selectTab(TabLabel.PROFILE)
        composeTestRule.onNodeWithText(ScreenText.PROFILE).assertIsDisplayed()

        composeTestRule.selectTab(TabLabel.HOME)
        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun profileSections_keepTheProfileTabHighlighted() {
        composeTestRule.openPersonalRecords()
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE).assertIsSelected()

        composeTestRule.goBack()
        composeTestRule.openProfileDestination("Glucose levels")
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE).assertIsSelected()

        composeTestRule.goBack()
        composeTestRule.openProfileDestination("Appointments")
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE).assertIsSelected()
    }

    @Test
    fun instructions_keepsTheTabItWasOpenedFrom() {
        // Opened from the home screen: Home stays highlighted.
        composeTestRule.openHome()
        composeTestRule.onNodeWithContentDescription("Learn").performClick()
        composeTestRule.onNodeWithText(ScreenText.INSTRUCTIONS).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(TabLabel.HOME).assertIsSelected()

        // Opened from the profile screen: Profile stays highlighted.
        composeTestRule.goBack()
        composeTestRule.selectTab(TabLabel.PROFILE)
        composeTestRule.openProfileDestination("Instruction Section")
        composeTestRule.onNodeWithContentDescription(TabLabel.PROFILE).assertIsSelected()
        composeTestRule.onNodeWithContentDescription(TabLabel.HOME).assertIsNotSelected()
    }

    @Test
    fun repeatedTabSwitching_doesNotGrowTheBackStack() {
        composeTestRule.openHome()

        composeTestRule.selectTab(TabLabel.ALERTS)
        composeTestRule.selectTab(TabLabel.PROFILE)
        composeTestRule.selectTab(TabLabel.HOME)
        composeTestRule.selectTab(TabLabel.ALERTS)

        // One back press returns to Home even though the tabs were tapped many
        // times; the stack only holds the current tab.
        Espresso.pressBack()

        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    private fun assertBarIsClickable() {
        listOf(TabLabel.HOME, TabLabel.ALERTS, TabLabel.PROFILE).forEach { label ->
            composeTestRule.onNodeWithContentDescription(label)
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()
        }
    }
}
