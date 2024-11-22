package com.example.cursorreminder.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cursorreminder.R
import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.presentation.ui.theme.CursorReminderTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmScreen(
    modifier: Modifier = Modifier,
    reminder: Reminder,
    onDismiss: () -> Unit,
    onMarkTaken: () -> Unit,
    isDialog: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if(isDialog) Arrangement.spacedBy(16.dp) else Arrangement.Center
    ) {
        // Top Icon
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = stringResource(R.string.alarm_time_for_medication),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Medication name
        Text(
            text = reminder.medicationName,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        // Medication dosage
        if (reminder.dosage.isNotBlank()) {
            Text(
                text = reminder.dosage,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Time
        Text(
            text = reminder.time.format(DateTimeFormatter.ofPattern("hh:mm a")),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(if (isDialog) 16.dp else 32.dp))

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mark as taken button
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onMarkTaken()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_mark_taken),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Dismiss button
            FilledTonalButton(
                modifier = Modifier.weight(1f),
                onClick = onDismiss,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.action_dismiss),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onMarkTaken: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            AlarmScreen(
                reminder = reminder,
                onDismiss = onDismiss,
                onMarkTaken = onMarkTaken,
                isDialog = true
            )
        }
    }
}

@Preview
@Composable
private fun AlarmScreenDDialogPreview() {
    CursorReminderTheme {
        AlarmDialog(
            reminder = Reminder(
                medicationName = "Vitamin C",
                time = LocalDateTime.now()
            ),
            onDismiss = {},
        ) { }
    }

}

@Preview
@Composable
private fun AlarmScreenPreview() {
    CursorReminderTheme {
        AlarmScreen(
            reminder = Reminder(
                medicationName = "Vitamin C",
                time = LocalDateTime.now()
            ),
            onDismiss = {},
            onMarkTaken = {}
        )
    }

}