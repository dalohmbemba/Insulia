package com.example.insulia.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.NotificationItem
import com.example.insulia.data.NotificationReconciler
import com.example.insulia.data.DateFormats
import com.example.insulia.navigation.Screen
import com.example.insulia.ui.components.InsuliaBottomBar
import com.example.insulia.ui.components.InsuliaCard
import com.example.insulia.ui.components.PageHeader

/** Notifications grouped by read state with a visible category tag on each item. */
@Composable
fun AlertsScreen(
    notifications: List<NotificationItem>,
    onBack: () -> Unit,
    onSelectTab: (Screen) -> Unit,
    onMarkRead: (String) -> Unit,
    selectedTab: Screen
) {
    val ordered = notifications.sortedByDescending { it.timestamp }
    val unread = ordered.filterNot { it.isRead }
    val read = ordered.filter { it.isRead }
    var showingRead by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageHeader(
            title = stringResource(R.string.page_notifications),
            onBack = onBack
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showingRead = false },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showingRead) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary,
                    contentColor = if (showingRead) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.notifications_unread_group, unread.size))
            }
            Button(
                onClick = { showingRead = true },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showingRead) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (showingRead) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.notifications_read_group, read.size))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            val visibleNotifications = if (showingRead) read else unread
            if (visibleNotifications.isEmpty()) {
                InsuliaCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            if (showingRead) R.string.notifications_read_empty
                            else R.string.notifications_unread_empty
                        ),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                visibleNotifications.forEach { item ->
                    NotificationCard(item = item, onMarkRead = { onMarkRead(item.id) })
                }
            }
        }

        InsuliaBottomBar(
            selected = selectedTab,
            onSelect = onSelectTab
        )
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onMarkRead: () -> Unit
) {
    val titleText = item.title ?: if (item.titleRes != 0) stringResource(item.titleRes) else ""
    val bodyText = item.body ?: if (item.bodyRes != 0) stringResource(item.bodyRes) else ""
    InsuliaCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = titleText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                NotificationTag(
                    label = stringResource(notificationTypeLabel(item.type))
                )
            }

            if (bodyText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bodyText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${DateFormats.shortDate(item.timestamp)} · ${DateFormats.timeLabel(item.timestamp)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (!item.isRead) {
                    TextButton(onClick = onMarkRead) {
                        Text(stringResource(R.string.notifications_mark_read))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationTag(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@StringRes
private fun notificationTypeLabel(type: String?): Int = when (type) {
    NotificationReconciler.TYPE_GLUCOSE -> R.string.notification_type_glucose
    NotificationReconciler.TYPE_MEDICATION -> R.string.notification_type_medication
    NotificationReconciler.TYPE_APPOINTMENT -> R.string.notification_type_appointment
    else -> R.string.notification_type_other
}
