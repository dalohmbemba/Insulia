package com.example.insulia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.NotificationItem
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.InsuliaCard
import com.example.insulia.ui.components.PageHeader

/** The notifications screen. */
@Composable
fun AlertsScreen(
    notifications: List<NotificationItem>,
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    selectedTab: Screen
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageHeader(
            title = stringResource(R.string.page_notifications),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            notifications.sortedByDescending { it.timestamp }.forEach { item ->
                InsuliaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val titleText = item.title ?: if (item.titleRes != 0) stringResource(item.titleRes) else ""
                        val bodyText = item.body ?: if (item.bodyRes != 0) stringResource(item.bodyRes) else ""

                        if (titleText.isNotEmpty()) {
                            Text(
                                text = titleText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (bodyText.isNotEmpty()) {
                            Text(
                                text = bodyText,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = stringResource(R.string.notifications_time_label),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        InsuliaBottomBar(
            selected = selectedTab,
            onSelect = onSelectTab
        )
    }
}
