package com.example.insulia.navigation

sealed interface Screen {
    data object Welcome : Screen
    data object Home : Screen
    data object Alerts : Screen
    data object Profile : Screen
    data object PersonalRecords : Screen
    data object GlucoseLevels : Screen
    data object FoodNotes : Screen
    data object Instructions : Screen
    data class DiabetesResource(val title: String, val url: String) : Screen
    data object Appointments : Screen
}

val BOTTOM_NAV_SCREENS = listOf(Screen.Home, Screen.Alerts, Screen.Profile)
