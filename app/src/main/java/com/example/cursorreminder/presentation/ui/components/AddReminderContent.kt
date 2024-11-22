package com.example.cursorreminder.presentation.ui.components

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursorreminder.R
import com.example.cursorreminder.presentation.viewmodel.ReminderViewModel
import com.example.cursorreminder.presentation.viewmodel.ScheduleType
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AddReminderContent(
    viewModel: ReminderViewModel,
    onRequestPermission: () -> Unit,
    onDone: () -> Unit,
) {
    val medicationName by viewModel.medicationName.collectAsStateWithLifecycle()
    val selectedTime by viewModel.selectedTime.collectAsStateWithLifecycle()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    val scheduleType by viewModel.scheduleType.collectAsStateWithLifecycle()
    val selectedDays by viewModel.selectedDays.collectAsStateWithLifecycle()
    var showCustomScheduleDialog by remember { mutableStateOf(false) }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val newDateTime = selectedTime.with(LocalTime.of(hour, minute))
                viewModel.updateSelectedTime(newDateTime)
            },
            selectedTime.hour,
            selectedTime.minute,
            true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.title_add_reminder),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.subtitle_add_reminder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Medication Name Input
        OutlinedTextField(
            value = medicationName,
            onValueChange = { viewModel.updateMedicationName(it) },
            label = { Text(stringResource(R.string.label_medication_name)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Time Selection
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.label_reminder_time),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
                onClick = { timePickerDialog.show() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_access_time),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Schedule Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.label_schedule_options),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { viewModel.updateScheduleType(ScheduleType.DAILY) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (scheduleType == ScheduleType.DAILY)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_calendar_today),
                        contentDescription = null,
                        tint = if (scheduleType == ScheduleType.DAILY)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_daily),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (scheduleType == ScheduleType.DAILY)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                FilledTonalButton(
                    onClick = {
                        viewModel.updateScheduleType(ScheduleType.CUSTOM)
                        showCustomScheduleDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (scheduleType == ScheduleType.CUSTOM)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.icon_calendar_month),
                        contentDescription = null,
                        tint = if (scheduleType == ScheduleType.CUSTOM)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_custom),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (scheduleType == ScheduleType.CUSTOM)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Selected schedule info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(
                            if (scheduleType == ScheduleType.DAILY) R.drawable.icon_calendar_today
                            else R.drawable.icon_calendar_month
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = when (scheduleType) {
                            ScheduleType.DAILY -> stringResource(R.string.schedule_info_daily)
                            ScheduleType.CUSTOM -> if (selectedDays.isEmpty()) {
                                stringResource(R.string.schedule_info_select_days)
                            } else {
                                selectedDays.sortedBy { it.value }
                                    .joinToString(", ") {
                                        it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                    }
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Show selected days if custom schedule
            if (scheduleType == ScheduleType.CUSTOM && selectedDays.isNotEmpty()) {
                Text(
                    text = selectedDays.joinToString(", ") {
                        it.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Custom Schedule Dialog
        AnimatedVisibility(
            visible = showCustomScheduleDialog,
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 500),
                shrinkTowards = Alignment.Bottom
            )
        ) {
            AlertDialog(
                onDismissRequest = { showCustomScheduleDialog = false },
                title = {
                    Text(
                        text = stringResource(R.string.title_select_days),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayOfWeek.entries.forEach { day ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleDay(day) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day.getDisplayName(
                                        TextStyle.FULL,
                                        Locale.getDefault()
                                    ),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Checkbox(
                                    checked = selectedDays.contains(day),
                                    onCheckedChange = { viewModel.toggleDay(day) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showCustomScheduleDialog = false }
                    ) {
                        Text(stringResource(R.string.action_done))
                    }
                },
                shape = MaterialTheme.shapes.large
            )
        }
        // Add Button
        Button(
            onClick = {
                if (hasNotificationPermission) {
                    viewModel.addReminder()
                    onDone()
                } else {
                    showPermissionDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = medicationName.isNotBlank(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.action_add_reminder),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }


    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_permission_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_permission_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        onRequestPermission()
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(stringResource(R.string.action_grant_permission))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text(stringResource(R.string.action_later))
                }
            },
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}