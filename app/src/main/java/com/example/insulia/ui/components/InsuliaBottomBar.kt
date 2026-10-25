package com.example.insulia.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.insulia.R
import com.example.insulia.navigation.Screen

/**
 * The fixed bottom navigation bar.
 *
 * Insets are handled by the app root, so the bar itself does not add any
 * padding of its own.
 */
@Composable
fun InsuliaBottomBar(
    selected: Screen,
    onSelect: (Screen) -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        BottomBarItem(
            screen = Screen.Home,
            selected = selected,
            onSelect = onSelect,
            icon = { label ->
                Icon(Icons.Default.Home, contentDescription = label)
            },
            label = stringResource(R.string.nav_home)
        )

        BottomBarItem(
            screen = Screen.Alerts,
            selected = selected,
            onSelect = onSelect,
            icon = { label ->
                Icon(Icons.Default.Notifications, contentDescription = label)
            },
            label = stringResource(R.string.nav_alerts)
        )

        BottomBarItem(
            screen = Screen.Profile,
            selected = selected,
            onSelect = onSelect,
            icon = { label ->
                Icon(Icons.Default.Person, contentDescription = label)
            },
            label = stringResource(R.string.nav_profile)
        )
    }
}

@Composable
private fun RowScope.BottomBarItem(
    screen: Screen,
    selected: Screen,
    onSelect: (Screen) -> Unit,
    icon: @Composable (String) -> Unit,
    label: String
) {
    NavigationBarItem(
        selected = selected == screen,
        onClick = { onSelect(screen) },
        icon = { icon(label) },
        label = { Text(label) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
