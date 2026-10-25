package com.example.insulia

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.insulia.data.MealType
import com.example.insulia.data.ReadingContext
import com.example.insulia.ui.dialogs.BookingDialogTags
import com.example.insulia.ui.dialogs.FoodNoteDialogTags
import com.example.insulia.ui.dialogs.LogReadingDialogTags
import org.junit.Rule
import org.junit.Test

/**
 * Every button in the app responds to a click.
 *
 * Each test first checks that the control is displayed, enabled and has a click
 * action, then clicks it and verifies the visible result. Validation rules are
 * covered too, because a button that can be pressed when the form is invalid is
 * just as broken as one that does nothing.
 */
class InsuliaButtonsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun getStartedButton_respondsToClick() {
        composeTestRule.onNodeWithText(ScreenText.WELCOME)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        composeTestRule.onNodeWithText(ScreenText.HOME).assertIsDisplayed()
    }

    @Test
    fun homeQuickActionButtons_areEnabledAndClickable() {
        composeTestRule.openHome()

        listOf("Log reading", "Food note", "Learn").forEach { label ->
            composeTestRule.onNodeWithContentDescription(label)
                .performScrollTo()
                .assertIsDisplayed()
                .assertIsEnabled()
                .assertHasClickAction()
        }
    }

    @Test
    fun logReadingButton_opensDialogWithDisabledSave() {
        composeTestRule.openLogReadingDialog()

        composeTestRule.onNodeWithText("Save").assertIsDisplayed().assertIsNotEnabled()
        composeTestRule.onNodeWithText("Cancel").assertIsEnabled().assertHasClickAction()
    }

    @Test
    fun logReading_valueOutsideRange_showsErrorAndKeepsSaveDisabled() {
        composeTestRule.openLogReadingDialog()

        composeTestRule.onNodeWithTag(LogReadingDialogTags.VALUE).performTextInput("10")

        composeTestRule.onNodeWithText("Enter a value between 20 and 600").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()

        composeTestRule.onNodeWithTag(LogReadingDialogTags.VALUE).performTextReplacement("123")
        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun logReading_saveButton_recordsTheReading() {
        composeTestRule.openLogReadingDialog()

        composeTestRule.onNodeWithTag(LogReadingDialogTags.VALUE).performTextInput("123")
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.onNodeWithText("Log a glucose reading").assertDoesNotExist()
        composeTestRule.onNodeWithText("123 mg/dL").assertIsDisplayed()
    }

    @Test
    fun logReading_contextButton_changesTheSavedReading() {
        composeTestRule.openLogReadingDialog()

        composeTestRule.onNodeWithTag(LogReadingDialogTags.VALUE).performTextInput("100")
        composeTestRule.onNodeWithTag(LogReadingDialogTags.context(ReadingContext.BEDTIME))
            .performScrollTo()
            .assertIsNotSelected()
            .performClick()
            .assertIsSelected()

        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.onNode(hasText("Bedtime", substring = true)).assertExists()
    }

    @Test
    fun logReading_cancelButton_discardsTheReading() {
        composeTestRule.openLogReadingDialog()

        composeTestRule.onNodeWithTag(LogReadingDialogTags.VALUE).performTextInput("555")
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Log a glucose reading").assertDoesNotExist()
        composeTestRule.onNodeWithText("555 mg/dL").assertDoesNotExist()
    }

    @Test
    fun foodNoteButton_opensDialogWithDisabledSave() {
        composeTestRule.openFoodNoteDialog()

        composeTestRule.onNodeWithText("Save").assertIsDisplayed().assertIsNotEnabled()
        composeTestRule.onNodeWithText("Cancel").assertIsEnabled().assertHasClickAction()
    }

    @Test
    fun foodNote_saveButton_recordsTheNoteAndMeal() {
        composeTestRule.openFoodNoteDialog()

        composeTestRule.onNodeWithTag(FoodNoteDialogTags.NOTE)
            .performTextInput("Rice and vegetables")

        composeTestRule.onNodeWithTag(FoodNoteDialogTags.meal(MealType.LUNCH))
            .performScrollTo()
            .performClick()
            .assertIsSelected()

        composeTestRule.onNodeWithText("Save").assertIsEnabled().performClick()

        composeTestRule.onNodeWithText("Add a food note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Rice and vegetables").assertExists()
    }

    @Test
    fun foodNote_cancelButton_discardsTheNote() {
        composeTestRule.openFoodNoteDialog()

        composeTestRule.onNodeWithTag(FoodNoteDialogTags.NOTE)
            .performTextInput("Rice and vegetables")
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Add a food note").assertDoesNotExist()
        composeTestRule.onNodeWithText("Rice and vegetables").assertDoesNotExist()
    }

    @Test
    fun profileQuickActionButtons_openTheDialogsAsWell() {
        composeTestRule.openProfile()

        composeTestRule.onNodeWithContentDescription("Log reading")
            .performScrollTo()
            .assertHasClickAction()
            .performClick()
        composeTestRule.onNodeWithText("Log a glucose reading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithContentDescription("Food note")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithText("Add a food note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithContentDescription("Learn")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithText(ScreenText.INSTRUCTIONS).assertIsDisplayed()
    }

    @Test
    fun bookNowButton_opensTheFormWithSubmitDisabled() {
        composeTestRule.openAppointments()

        composeTestRule.onNodeWithText("Book now")
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        composeTestRule.onNodeWithText("Book an appointment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun booking_withoutDate_keepsSubmitDisabled() {
        composeTestRule.openBookingDialog()

        composeTestRule.onNodeWithTag(BookingDialogTags.PATIENT_NAME)
            .performTextInput("Madalo Mbemba")
        composeTestRule.onNodeWithTag(BookingDialogTags.CLINIC_NAME)
            .performTextInput("Queen Elizabeth Central Hospital")

        composeTestRule.onNodeWithText("Submit").assertIsNotEnabled()
    }

    @Test
    fun booking_visitTypeButtons_toggleTheSelection() {
        composeTestRule.openBookingDialog()

        composeTestRule.onNodeWithTag(BookingDialogTags.VISIT_TYPE_NORMAL)
            .performScrollTo()
            .assertIsSelected()

        composeTestRule.onNodeWithTag(BookingDialogTags.VISIT_TYPE_EMERGENCY)
            .performScrollTo()
            .performClick()
            .assertIsSelected()

        composeTestRule.onNodeWithTag(BookingDialogTags.VISIT_TYPE_NORMAL)
            .assertIsNotSelected()
    }

    @Test
    fun booking_cancelButton_closesTheForm() {
        composeTestRule.openBookingDialog()

        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.onNodeWithText("Book an appointment").assertDoesNotExist()
        composeTestRule.onNodeWithText(ScreenText.APPOINTMENTS).assertIsDisplayed()
    }

    @Test
    fun booking_completeForm_createsTheAppointment() {
        composeTestRule.openBookingDialog()

        composeTestRule.onNodeWithTag(BookingDialogTags.PATIENT_NAME)
            .performTextInput("Madalo Mbemba")
        composeTestRule.onNodeWithTag(BookingDialogTags.CLINIC_NAME)
            .performTextInput("Queen Elizabeth Central Hospital")

        composeTestRule.pickAppointmentDate()

        composeTestRule.onNodeWithText("Submit").assertIsEnabled().performClick()

        composeTestRule.onNodeWithText("Book an appointment").assertDoesNotExist()
        composeTestRule.onNode(hasText("Booking confirmed", substring = true)).assertExists()
        composeTestRule.onNodeWithText("Routine visit").assertExists()
    }

    @Test
    fun glucoseLevels_deleteButton_removesAReading() {
        composeTestRule.openGlucoseLevels()

        composeTestRule.onAllNodesWithText("145 mg/dL").assertCountEquals(2)

        composeTestRule.onAllNodesWithContentDescription("Delete reading")[0]
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        composeTestRule.onAllNodesWithText("145 mg/dL").assertCountEquals(1)
    }

    @Test
    fun backArrow_isClickableOnEverySubScreen() {
        composeTestRule.openHome()

        composeTestRule.selectTab(TabLabel.ALERTS)
        assertBackArrowReturnsTo(ScreenText.HOME)

        composeTestRule.selectTab(TabLabel.PROFILE)
        assertBackArrowReturnsTo(ScreenText.HOME)

        composeTestRule.openProfileDestination("Personal Records")
        assertBackArrowReturnsTo(ScreenText.PROFILE)

        composeTestRule.openProfileDestination("Glucose levels")
        assertBackArrowReturnsTo(ScreenText.PROFILE)

        composeTestRule.openProfileDestination("Appointments")
        assertBackArrowReturnsTo(ScreenText.PROFILE)

        composeTestRule.openProfileDestination("Instruction Section")
        assertBackArrowReturnsTo(ScreenText.PROFILE)
    }

    private fun assertBackArrowReturnsTo(expectedScreenText: String) {
        composeTestRule.onNodeWithContentDescription(GO_BACK)
            .assertIsDisplayed()
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        composeTestRule.onNodeWithText(expectedScreenText).assertIsDisplayed()
    }
}
