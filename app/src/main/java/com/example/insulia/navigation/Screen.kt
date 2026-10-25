package com.example.insulia.navigation

/**
 * Every screen the app can show.
 *
 * Screens are modelled as a sealed hierarchy rather than string keys so the
 * compiler catches typos and so `when` blocks over the destination are
 * exhaustive.
 */
sealed interface Screen {

    /** First screen the patient sees, with the "Get started" button. */
    data object Welcome : Screen

    /** Dashboard with the latest reading, quick actions and the trend chart. */
    data object Home : Screen

    /** Notifications list. */
    data object Alerts : Screen

    /** Profile overview and the entry points to the other sections. */
    data object Profile : Screen

    /** Saved patient information and diabetes records. */
    data object PersonalRecords : Screen

    /** Full list of recorded glucose readings. */
    data object GlucoseLevels : Screen

    /** Guidance for patients and caregivers. */
    data object Instructions : Screen

    /** Upcoming and previous clinic appointments. */
    data object Appointments : Screen
}

/** The screens reachable from the bottom navigation bar. */
val BOTTOM_NAV_SCREENS = listOf(Screen.Home, Screen.Alerts, Screen.Profile)
