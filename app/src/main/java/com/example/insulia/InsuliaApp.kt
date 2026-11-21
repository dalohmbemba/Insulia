package com.example.insulia

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insulia.data.Appointment
import com.example.insulia.data.InsuliaViewModel
import com.example.insulia.data.Medication
import com.example.insulia.data.ReadingContext
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.QuickActionType
import com.example.insulia.ui.dialogs.AccountDialog
import com.example.insulia.ui.dialogs.BookingDialog
import com.example.insulia.ui.dialogs.EditProfileDialog
import com.example.insulia.ui.dialogs.FoodNoteDialog
import com.example.insulia.ui.dialogs.LogReadingDialog
import com.example.insulia.ui.dialogs.MedicationDialog
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
    data object Account : AppDialog
    data object EditProfile : AppDialog
    data class MedicationEditor(val medication: Medication, val isNew: Boolean) : AppDialog
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
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen = backStack.last()

    // Every failed save or sync is surfaced instead of being swallowed.
    LaunchedEffect(viewModel.dataError) {
        val message = viewModel.dataError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            withDismissAction = true,
            duration = SnackbarDuration.Long
        )
        viewModel.clearDataError()
    }

    LaunchedEffect(viewModel.dataMessage) {
        val message = viewModel.dataMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
        viewModel.clearDataMessage()
    }

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

    fun startBooking(initial: Appointment? = null) {
        editingAppointment = initial
        dialog = AppDialog.Booking
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
                onDeleteFoodNote = viewModel::deleteFoodNote,
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
                accountEmail = viewModel.accountEmail,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                onQuickAction = ::handleQuickAction,
                onOpen = ::open,
                onEditProfile = { dialog = AppDialog.EditProfile },
                onManageAccount = { dialog = AppDialog.Account },
                selectedTab = selectedTab
            )

            Screen.PersonalRecords -> PersonalRecordsScreen(
                patient = viewModel.patient,
                latestReading = viewModel.latestReading,
                readingCount = viewModel.readings.size,
                foodNoteCount = viewModel.foodNotes.size,
                appointmentCount = viewModel.appointments.size,
                medications = viewModel.medications,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                onAddMedication = {
                    dialog = AppDialog.MedicationEditor(
                        medication = viewModel.newMedication(),
                        isNew = true
                    )
                },
                onEditMedication = { medication ->
                    dialog = AppDialog.MedicationEditor(medication = medication, isNew = false)
                },
                onDeleteMedication = { medication ->
                    viewModel.deleteMedication(medication.id)
                },
                onToggleMedication = { medication ->
                    viewModel.setMedicationActive(medication.id, !medication.isActive)
                },
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
                onBookAppointment = { startBooking() },
                onReschedule = { appointment -> startBooking(appointment) },
                onDeleteAppointment = { appointment ->
                    viewModel.deleteAppointment(appointment.id)
                },
                selectedTab = selectedTab
            )
        }

        when (val shownDialog = dialog) {
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
                initial = editingAppointment,
                onDismiss = {
                    editingAppointment = null
                    dialog = null
                },
                onBook = { patientName, clinicName, dateMillis, lastVisit, isEmergency ->
                    val editing = editingAppointment
                    if (editing == null) {
                        viewModel.bookAppointment(
                            patientName = patientName,
                            clinicName = clinicName,
                            dateMillis = dateMillis,
                            lastVisit = lastVisit,
                            isEmergency = isEmergency
                        )
                    } else {
                        viewModel.rescheduleAppointment(
                            id = editing.id,
                            patientName = patientName,
                            clinicName = clinicName,
                            dateMillis = dateMillis,
                            lastVisit = lastVisit,
                            isEmergency = isEmergency
                        )
                    }
                    showBookingConfirmation = true
                    editingAppointment = null
                    dialog = null
                }
            )

            AppDialog.Account -> AccountDialog(
                isAnonymous = viewModel.isAnonymousAccount,
                accountEmail = viewModel.accountEmail,
                busy = viewModel.authBusy,
                onDismiss = { dialog = null },
                onCreateAccount = viewModel::linkAccount,
                onSignIn = viewModel::signInToAccount,
                onResetPassword = viewModel::sendPasswordReset
            )

            AppDialog.EditProfile -> EditProfileDialog(
                patient = viewModel.patient,
                onDismiss = { dialog = null },
                onSave = { updated ->
                    viewModel.updateProfile(updated)
                    dialog = null
                }
            )

            is AppDialog.MedicationEditor -> MedicationDialog(
                initial = shownDialog.medication,
                isNew = shownDialog.isNew,
                onDismiss = { dialog = null },
                onSave = { medication ->
                    viewModel.addOrUpdateMedication(medication)
                    dialog = null
                }
            )

            null -> Unit
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
