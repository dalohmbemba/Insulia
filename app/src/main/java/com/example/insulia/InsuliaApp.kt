package com.example.insulia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insulia.data.InsuliaViewModel
import com.example.insulia.data.ReadingContext
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.QuickActionType
import com.example.insulia.ui.dialogs.BookingDialog
import com.example.insulia.ui.dialogs.FoodNoteDialog
import com.example.insulia.ui.dialogs.LogReadingDialog
import com.example.insulia.ui.screens.AlertsScreen
import com.example.insulia.ui.screens.AppointmentsScreen
import com.example.insulia.ui.screens.GlucoseLevelsScreen
import com.example.insulia.ui.screens.HomeScreen
import com.example.insulia.ui.screens.InstructionSectionScreen
import com.example.insulia.ui.screens.PersonalRecordsScreen
import com.example.insulia.ui.screens.ProfileScreen
import com.example.insulia.ui.screens.WelcomeScreen

/** The dialogs the app can show on top of the current screen. */
private sealed interface AppDialog {
    data object LogReading : AppDialog
    data object FoodNote : AppDialog
    data object Booking : AppDialog
}

/**
 * Root of the app.
 *
 * Navigation uses a back stack so that both the in-app back arrow and the
 * system back gesture return to the previous screen. Selecting a bottom
 * navigation tab replaces the stack instead of growing it, which stops the
 * stack from filling up with repeated screens.
 */
@Composable
fun InsuliaApp(
    viewModel: InsuliaViewModel = viewModel()
) {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Welcome) }
    var dialog by remember { mutableStateOf<AppDialog?>(null) }
    var showBookingConfirmation by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf<Screen>(Screen.Home) }

    val currentScreen = backStack.last()

    /**
     * The tab that is highlighted in the bottom navigation.
     *
     * The profile sections always highlight the profile tab, everything else
     * keeps the tab the patient came from.
     */
    val selectedTab = when (currentScreen) {
        Screen.Profile,
        Screen.PersonalRecords,
        Screen.GlucoseLevels,
        Screen.Appointments -> Screen.Profile

        else -> currentTab
    }

    /** Opens a screen on top of the current one. */
    fun open(screen: Screen) {
        if (backStack.last() != screen) {
            backStack.add(screen)
        }
    }

    /** Returns to the previous screen. */
    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    /** Shows a screen without keeping the previous one in the stack. */
    fun replaceRoot(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
    }

    /** Switches to a bottom navigation tab. */
    fun selectTab(tab: Screen) {
        currentTab = tab
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
        if (backStack.last() != tab) {
            backStack.add(tab)
        }
    }

    fun handleQuickAction(action: QuickActionType) {
        when (action) {
            QuickActionType.LOG_READING -> dialog = AppDialog.LogReading
            QuickActionType.FOOD_NOTE -> dialog = AppDialog.FoodNote
            QuickActionType.LEARN -> open(Screen.Instructions)
        }
    }

    // The system back gesture follows the same stack as the in-app back arrow.
    BackHandler(enabled = backStack.size > 1) {
        goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        when (currentScreen) {
            Screen.Welcome -> WelcomeScreen(
                onGetStarted = { replaceRoot(Screen.Home) }
            )

            Screen.Home -> HomeScreen(
                patient = viewModel.patient,
                latestReading = viewModel.latestReading,
                recentReadings = viewModel.recentReadings,
                latestFoodNote = viewModel.latestFoodNote,
                onQuickAction = ::handleQuickAction,
                onSelectTab = ::selectTab,
                selectedTab = selectedTab
            )

            Screen.Alerts -> AlertsScreen(
                notifications = viewModel.notifications,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                selectedTab = selectedTab
            )

            Screen.Profile -> ProfileScreen(
                patient = viewModel.patient,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                onQuickAction = ::handleQuickAction,
                onOpen = ::open,
                selectedTab = selectedTab
            )

            Screen.PersonalRecords -> PersonalRecordsScreen(
                patient = viewModel.patient,
                latestReading = viewModel.latestReading,
                readingCount = viewModel.readings.size,
                foodNoteCount = viewModel.foodNotes.size,
                appointmentCount = viewModel.appointments.size,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                selectedTab = selectedTab
            )

            Screen.GlucoseLevels -> GlucoseLevelsScreen(
                readings = viewModel.readings,
                averageReading = viewModel.averageReading,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                onDeleteReading = viewModel::deleteReading,
                selectedTab = selectedTab
            )

            Screen.Instructions -> InstructionSectionScreen(
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                selectedTab = selectedTab
            )

            Screen.Appointments -> AppointmentsScreen(
                upcoming = viewModel.upcomingAppointment,
                previous = viewModel.previousAppointments,
                showConfirmation = showBookingConfirmation,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                onBookAppointment = { dialog = AppDialog.Booking },
                selectedTab = selectedTab
            )
        }

        when (dialog) {
            AppDialog.LogReading -> LogReadingDialog(
                onDismiss = { dialog = null },
                onSave = { value, context: ReadingContext ->
                    viewModel.addReading(value, context)
                    dialog = null
                }
            )

            AppDialog.FoodNote -> FoodNoteDialog(
                onDismiss = { dialog = null },
                onSave = { meal, note ->
                    viewModel.addFoodNote(meal, note)
                    dialog = null
                }
            )

            AppDialog.Booking -> BookingDialog(
                onDismiss = { dialog = null },
                onBook = { patientName, clinicName, dateMillis, lastVisit, isEmergency ->
                    viewModel.bookAppointment(
                        patientName = patientName,
                        clinicName = clinicName,
                        dateMillis = dateMillis,
                        lastVisit = lastVisit,
                        isEmergency = isEmergency
                    )
                    showBookingConfirmation = true
                    dialog = null
                }
            )

            null -> Unit
        }
    }
}
