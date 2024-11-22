package com.example.cursorreminder.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursorreminder.R
import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.presentation.ui.theme.completedColor
import com.example.cursorreminder.presentation.ui.theme.completedContentColor
import com.example.cursorreminder.presentation.viewmodel.ReminderViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayReminderItem(
    reminder: Reminder,
    onToggleCompletion: () -> Unit,
    viewModel: ReminderViewModel,
) {
    val isCompletedToday = reminder.completedDates.contains(LocalDate.now())
    val activeAlarms by viewModel.activeAlarms.collectAsStateWithLifecycle()
    val isAlarmActive = activeAlarms.contains(reminder.id)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCompletedToday) completedColor
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = reminder.medicationName,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isCompletedToday) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (isCompletedToday) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.taken),
//                                tint = completedContentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = reminder.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reminder.isEnabled && isAlarmActive) {
                        // Dismiss button
                        IconButton(
                            onClick = { viewModel.dismissAlarm(reminder) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.content_description_dismiss_alarm),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        // Mark as taken button
                        IconButton(
                            onClick = onToggleCompletion,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isCompletedToday) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.secondaryContainer,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(R.string.content_description_mark_taken),
                                tint = if (isCompletedToday) completedColor
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Enable/Disable Switch
                    Switch(
                        checked = reminder.isEnabled,
                        onCheckedChange = { viewModel.toggleReminder(reminder) },
                        thumbContent = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.content_description_toggle_reminder),
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = if (reminder.isEnabled) {
                                    if (isCompletedToday) Color.White
                                    else MaterialTheme.colorScheme.onPrimary
                                } else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isCompletedToday) completedContentColor
                            else MaterialTheme.colorScheme.primary,
                            checkedTrackColor = if (isCompletedToday) completedColor
                            else MaterialTheme.colorScheme.primaryContainer,
                            checkedBorderColor = if (isCompletedToday) completedContentColor
                            else MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}