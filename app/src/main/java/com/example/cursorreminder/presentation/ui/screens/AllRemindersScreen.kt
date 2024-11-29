package com.example.cursorreminder.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cursorreminder.R
import com.example.cursorreminder.presentation.ui.components.AddReminderContent
import com.example.cursorreminder.presentation.ui.components.DateHeader
import com.example.cursorreminder.presentation.ui.components.GroupDate
import com.example.cursorreminder.presentation.ui.components.SwipeToDeleteContainer
import com.example.cursorreminder.presentation.ui.components.TodayReminderItem
import com.example.cursorreminder.presentation.viewmodel.ReminderViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRemindersScreen(
    viewModel: ReminderViewModel,
    onRequestPermission: () -> Unit,
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

//    // Group reminders by day
//    val groupedReminders = reminders.groupBy { reminder ->
//        reminder.scheduledDays
//    }

// Group reminders by date
    val groupedReminders = remember(reminders) {
        reminders.groupBy { reminder ->
            val today = LocalDate.now()
            val nextOccurrence = reminder.scheduledDays.minOfOrNull { day ->
                getNextOccurrence(
                    reminder.time,
                    day
                ).toLocalDate()
            } ?: return@groupBy GroupDate.Future
            when {
                nextOccurrence.isEqual(today) -> GroupDate.Today
                nextOccurrence.isEqual(today.plusDays(1)) -> GroupDate.Tomorrow
                nextOccurrence.isAfter(today.plusDays(1)) -> GroupDate.Future
                else -> GroupDate.Past
            }
        }.toSortedMap(compareBy { it.ordinal })
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.all_medications),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = stringResource(R.string.all_medications_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0)
            )
        },
//        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.content_description_add)
                )
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            EmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp) // For FAB
            ) {
                groupedReminders.forEach { (dateGroup, dateReminders) ->
                    item {
                        DateHeader(
                            dateGroup = dateGroup,
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 24.dp,
                                bottom = 12.dp
                            )
                        )
                    }

                    items(
                        items = dateReminders.sortedBy { it.time },
                        key = { it.id }
                    ) { reminder ->
                        SwipeToDeleteContainer(
                            item = reminder,
                            onDelete = { viewModel.deleteReminder(reminder) }
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                TodayReminderItem(
                                    reminder = reminder,
                                    viewModel = viewModel
                                )
                            }
                        }
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

// Helper function to get next occurrence
private fun getNextOccurrence(time: LocalDateTime, dayOfWeek: DayOfWeek): LocalDateTime {
    var nextTime = time
    while (nextTime.dayOfWeek != dayOfWeek || nextTime.toLocalDate().isBefore(LocalDate.now())) {
        nextTime = nextTime.plusDays(1)
    }
    return nextTime
}

@Composable
private fun DayHeader(days: List<DayOfWeek>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(
                text = formatScheduledDays(days),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Divider(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.no_medications),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.add_medication_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
        )
    }
}

@Composable
private fun formatScheduledDays(days: List<DayOfWeek>): String {
    return when {
        days.size == 7 -> stringResource(R.string.every_day)
        days.size == 5 && !days.contains(DayOfWeek.SATURDAY) && !days.contains(DayOfWeek.SUNDAY) ->
            stringResource(R.string.weekdays)

        days.size == 2 && days.contains(DayOfWeek.SATURDAY) && days.contains(DayOfWeek.SUNDAY) ->
            stringResource(R.string.weekends)

        else -> days.joinToString(", ") {
            it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }
    }
}