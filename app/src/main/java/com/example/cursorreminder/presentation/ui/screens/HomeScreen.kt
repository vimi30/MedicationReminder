package com.example.cursorreminder.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursorreminder.R
import com.example.cursorreminder.presentation.ui.components.AddReminderContent
import com.example.cursorreminder.presentation.ui.components.SwipeToDeleteContainer
import com.example.cursorreminder.presentation.ui.components.TodayReminderItem
import com.example.cursorreminder.presentation.ui.components.WeekCalendar
import com.example.cursorreminder.presentation.viewmodel.ReminderViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ReminderViewModel,
    onRequestPermission: () -> Unit,
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val completedDates by viewModel.completedDates.collectAsStateWithLifecycle()
    val activeAlarms by viewModel.activeAlarms.collectAsStateWithLifecycle()
    val todayReminders by viewModel.todayReminders.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showBottomSheet by remember { mutableStateOf(false) }

    // Find the active reminder and show alarm dialog
    reminders.find { reminder ->
        activeAlarms.contains(reminder.id)
    }?.let { activeReminder ->
        AlarmDialog(
            reminder = activeReminder,
            onDismiss = { viewModel.dismissAlarm(activeReminder) },
            onMarkTaken = { viewModel.toggleCompletion(activeReminder) }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.content_description_add)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Week Calendar
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                WeekCalendar(
                    completedDates = completedDates,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Today's Reminders
            Text(
                text = stringResource(R.string.title_todays_medications),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = todayReminders,
                    key = { it.id }
                ) { reminder ->
                    SwipeToDeleteContainer(
                        item = reminder,
                        onDelete = { viewModel.deleteReminder(reminder) }
                    ) {
                        TodayReminderItem(
                            reminder = reminder,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                AddReminderContent(
                    viewModel = viewModel,
                    onRequestPermission = onRequestPermission,
                    onDone = { showBottomSheet = false }
                )
            }
        }
    }
}
