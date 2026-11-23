package com.example.insulia

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import com.example.insulia.ui.screens.AuthScreen
import com.example.insulia.ui.screens.AppointmentsScreen
import com.example.insulia.ui.screens.FoodNotesScreen
import com.example.insulia.ui.screens.DiabetesResourceScreen
import com.example.insulia.ui.screens.GlucoseLevelsScreen
import com.example.insulia.ui.screens.HomeScreen
import com.example.insulia.ui.screens.InstructionSectionScreen
import com.example.insulia.ui.screens.PersonalRecordsScreen
import com.example.insulia.ui.screens.ProfileScreen
import com.example.insulia.ui.screens.WelcomeScreen

private sealed interface AppDialog {
    data object LogReading : AppDialog
    data object FoodNote : AppDialog
    data object Booking : AppDialog
    data object EditProfile : AppDialog
    data object Account : AppDialog
    data class MedicationEditor(val medication: Medication, val isNew: Boolean) : AppDialog
}

@Composable
fun InsuliaApp(viewModel: InsuliaViewModel = viewModel()) {
    val backStack = remember(viewModel) {
        mutableStateListOf(if (viewModel.isAuthenticated) Screen.Home else Screen.Welcome)
    }
    var dialog by remember { mutableStateOf<AppDialog?>(null) }
    var showAuthScreen by remember { mutableStateOf(false) }
    var showBookingConfirmation by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf<Screen>(Screen.Home) }
    val instructionScrollState = rememberScrollState()
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currentScreen = backStack.last()

    LaunchedEffect(viewModel.dataError) {
        val message = viewModel.dataError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message, withDismissAction = true, duration = SnackbarDuration.Long)
        viewModel.clearDataError()
    }

    LaunchedEffect(viewModel.dataMessage) {
        val message = viewModel.dataMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        viewModel.clearDataMessage()
    }

    LaunchedEffect(viewModel.isAuthenticated) {
        if (viewModel.isAuthenticated) {
            dialog = null
            viewModel.clearAuthFormError()
            if (backStack.last() == Screen.Welcome) {
                backStack.clear()
                backStack.add(Screen.Home)
            }
        }
    }

    val selectedTab = when (currentScreen) {
        Screen.Profile, Screen.PersonalRecords, Screen.GlucoseLevels,
        Screen.Appointments, Screen.FoodNotes -> Screen.Profile
        else -> currentTab
    }

    fun open(screen: Screen) {
        if (backStack.last() != screen) backStack.add(screen)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun replaceRoot(screen: Screen) {
        backStack.clear()
        backStack.add(screen)
    }

    fun selectTab(tab: Screen) {
        currentTab = tab
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        if (backStack.last() != tab) backStack.add(tab)
    }

    fun handleQuickAction(action: QuickActionType) {
        when (action) {
            QuickActionType.LOG_READING -> dialog = AppDialog.LogReading
            QuickActionType.FOOD_NOTE -> dialog = AppDialog.FoodNote
            QuickActionType.APPOINTMENTS -> open(Screen.Appointments)
            QuickActionType.LEARN -> open(Screen.Instructions)
        }
    }

    fun startBooking(initial: Appointment? = null) {
        editingAppointment = initial
        dialog = AppDialog.Booking
    }

    BackHandler(enabled = showAuthScreen || backStack.size > 1) {
        if (showAuthScreen) showAuthScreen = false else goBack()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        when (currentScreen) {
            Screen.Welcome -> WelcomeScreen(onGetStarted = { showAuthScreen = true })

            Screen.Home -> HomeScreen(
                patient = viewModel.patient,
                latestReading = viewModel.latestReading,
                recentReadings = viewModel.recentReadings,
                latestFoodNote = viewModel.latestFoodNote,
                foodNoteCount = viewModel.foodNotes.size,
                onQuickAction = ::handleQuickAction,
                onSelectTab = ::selectTab,
                onOpenFoodNotes = { open(Screen.FoodNotes) },
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
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                onOpen = ::open,
                onEditProfile = { dialog = AppDialog.EditProfile },
                isAnonymous = viewModel.isAnonymousAccount,
                onManageAccount = { dialog = AppDialog.Account },
                onLogout = {
                    viewModel.signOut { replaceRoot(Screen.Welcome) }
                },
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
                onAddMedication = { dialog = AppDialog.MedicationEditor(viewModel.newMedication(), true) },
                onEditMedication = { dialog = AppDialog.MedicationEditor(it, false) },
                onDeleteMedication = { viewModel.deleteMedication(it.id) },
                onToggleMedication = { viewModel.setMedicationActive(it.id, !it.isActive) },
                selectedTab = selectedTab
            )

            Screen.GlucoseLevels -> GlucoseLevelsScreen(
                viewModel.readings, viewModel.averageReading, ::goBack, ::selectTab,
                viewModel::deleteReading, selectedTab
            )

            Screen.FoodNotes -> FoodNotesScreen(
                notes = viewModel.foodNotes,
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                onAdd = { dialog = AppDialog.FoodNote },
                onDelete = viewModel::deleteFoodNote,
                selectedTab = selectedTab
            )

            Screen.Instructions -> InstructionSectionScreen(
                onBack = ::goBack,
                onSelectTab = ::selectTab,
                selectedTab = selectedTab,
                diabetesType = viewModel.patient.diabetesType,
                scrollState = instructionScrollState,
                onOpenResource = { title, url -> open(Screen.DiabetesResource(title, url)) }
            )

            is Screen.DiabetesResource -> DiabetesResourceScreen(
                title = currentScreen.title,
                url = currentScreen.url,
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
                onReschedule = ::startBooking,
                onDeleteAppointment = { viewModel.deleteAppointment(it.id) },
                selectedTab = selectedTab
            )
        }

        when (val shownDialog = dialog) {
            AppDialog.LogReading -> LogReadingDialog({ dialog = null }) { value, context ->
                viewModel.addReading(value, context); dialog = null
            }
            AppDialog.FoodNote -> FoodNoteDialog({ dialog = null }) { meal, note ->
                viewModel.addFoodNote(meal, note); dialog = null
            }
            AppDialog.Booking -> BookingDialog(
                initial = editingAppointment,
                onDismiss = { editingAppointment = null; dialog = null },
                onBook = { patientName, clinicName, dateMillis, lastVisit, isEmergency ->
                    val editing = editingAppointment
                    if (editing == null) viewModel.bookAppointment(patientName, clinicName, dateMillis, lastVisit, isEmergency)
                    else viewModel.rescheduleAppointment(editing.id, patientName, clinicName, dateMillis, lastVisit, isEmergency)
                    showBookingConfirmation = true
                    editingAppointment = null
                    dialog = null
                }
            )
            AppDialog.EditProfile -> EditProfileDialog(viewModel.patient, { dialog = null }) {
                viewModel.updateProfile(it); dialog = null
            }
            AppDialog.Account -> AccountDialog(
                isAnonymous = viewModel.isAnonymousAccount,
                accountEmail = viewModel.accountEmail,
                busy = viewModel.authBusy,
                errorMessage = viewModel.authFormError,
                onDismiss = { dialog = null; viewModel.clearAuthFormError() },
                onCreateAccount = viewModel::linkAccount,
                onSignIn = viewModel::signInToAccount,
                onResetPassword = viewModel::sendPasswordReset
            )
            is AppDialog.MedicationEditor -> MedicationDialog(
                shownDialog.medication, shownDialog.isNew, { dialog = null }
            ) { viewModel.addOrUpdateMedication(it); dialog = null }
            null -> Unit
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(16.dp))

        if (currentScreen == Screen.Welcome && showAuthScreen) {
            AuthScreen(
                busy = viewModel.authBusy,
                errorMessage = viewModel.authFormError,
                onClearError = viewModel::clearAuthFormError,
                onCreateAccount = viewModel::linkAccount,
                onSignIn = viewModel::signInToAccount,
                onResetPassword = viewModel::sendPasswordReset
            )
        }

    }
}
