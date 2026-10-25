package com.example.insulia

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo

/**
 * Text that identifies each screen of the app.
 *
 * The tests use the same visible text a patient would look for, so a rename in
 * `strings.xml` fails the tests instead of silently making them meaningless.
 */
object ScreenText {
    const val WELCOME = "GET STARTED"
    const val HOME = "Latest Glucose level"
    const val ALERTS = "Notifications"
    const val PROFILE = "Profile overview"
    const val PERSONAL_RECORDS = "Saved patient information"
    const val GLUCOSE_LEVELS = "All readings you have recorded"
    const val INSTRUCTIONS = "Guidance for patients and caregivers"
    const val APPOINTMENTS = "Upcoming"
}

/** The content descriptions of the three bottom navigation items. */
object TabLabel {
    const val HOME = "Home"
    const val ALERTS = "Alerts"
    const val PROFILE = "Profile"
}

/** The content description of the back arrow in the page header. */
const val GO_BACK = "Go back"

/**
 * Shared navigation helpers.
 *
 * Every helper starts from the welcome screen (or from the screen it is named
 * after) and asserts the destination, so a failure points at the transition
 * that broke rather than at an unrelated later assertion.
 */
fun ComposeTestRule.openHome() {
    onNodeWithText(ScreenText.WELCOME).performClick()
    onNodeWithText(ScreenText.HOME).assertExists()
}

/** Taps an item in the bottom navigation bar. */
fun ComposeTestRule.selectTab(label: String) {
    onNodeWithContentDescription(label).performClick()
}

fun ComposeTestRule.openAlerts() {
    openHome()
    selectTab(TabLabel.ALERTS)
    onNodeWithText(ScreenText.ALERTS).assertExists()
}

fun ComposeTestRule.openProfile() {
    openHome()
    selectTab(TabLabel.PROFILE)
    onNodeWithText(ScreenText.PROFILE).assertExists()
}

/** Taps one of the entries in the "Profile destinations" list. */
fun ComposeTestRule.openProfileDestination(title: String) {
    onNodeWithText(title).performScrollTo().performClick()
}

fun ComposeTestRule.openPersonalRecords() {
    openProfile()
    openProfileDestination("Personal Records")
    onNodeWithText(ScreenText.PERSONAL_RECORDS).assertExists()
}

fun ComposeTestRule.openGlucoseLevels() {
    openProfile()
    openProfileDestination("Glucose levels")
    onNodeWithText(ScreenText.GLUCOSE_LEVELS).assertExists()
}

fun ComposeTestRule.openAppointments() {
    openProfile()
    openProfileDestination("Appointments")
    onNodeWithText(ScreenText.APPOINTMENTS).assertExists()
}

fun ComposeTestRule.openInstructionsFromProfile() {
    openProfile()
    openProfileDestination("Instruction Section")
    onNodeWithText(ScreenText.INSTRUCTIONS).assertExists()
}

/** Taps the back arrow in the page header. */
fun ComposeTestRule.goBack() {
    onNodeWithContentDescription(GO_BACK).performClick()
}

fun ComposeTestRule.openLogReadingDialog() {
    openHome()
    onNodeWithContentDescription("Log reading").performClick()
    onNodeWithText("Log a glucose reading").assertExists()
}

fun ComposeTestRule.openFoodNoteDialog() {
    openHome()
    onNodeWithContentDescription("Food note").performClick()
    onNodeWithText("Add a food note").assertExists()
}

fun ComposeTestRule.openBookingDialog() {
    openAppointments()
    onNodeWithText("Book now").performScrollTo().performClick()
    onNodeWithText("Book an appointment").assertExists()
}

/**
 * Picks [day] of the displayed month in the Material date picker.
 *
 * The day cell is the clickable node whose text is exactly the day number, so
 * this does not accidentally match the date printed in the dialog title.
 */
fun ComposeTestRule.pickAppointmentDate(day: String = "15") {
    onNodeWithContentDescription("Pick a date").performScrollTo().performClick()
    onAllNodes(hasText(day) and hasClickAction())[0].performClick()
    onNodeWithText("OK").performClick()
}
