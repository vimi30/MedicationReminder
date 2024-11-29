package com.example.cursorreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.presentation.ui.screens.AlarmScreen
import com.example.cursorreminder.presentation.ui.theme.CursorReminderTheme
import com.example.cursorreminder.presentation.viewmodel.ReminderViewModel
import com.example.cursorreminder.receiver.AlarmReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    private val viewModel: ReminderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val reminderId = intent.getLongExtra(AlarmReceiver.EXTRA_REMINDER_ID, -1)
        val medicationName = intent.getStringExtra(AlarmReceiver.EXTRA_MEDICATION_NAME) ?: ""
        val medicationDosage = intent.getStringExtra(AlarmReceiver.EXTRA_MEDICATION_DOSAGE) ?: ""

        // Create a temporary reminder object for the alarm screen
        val reminder = Reminder(
            id = reminderId,
            medicationName = medicationName,
            dosage = medicationDosage,
            time = LocalDateTime.now(),
        )

        setContent {
            CursorReminderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmScreen(
                        reminder = reminder,
                        onDismiss = {
                            viewModel.dismissAlarm(reminder)
                            finish()
                        },
                        onMarkTaken = {
                            viewModel.toggleCompletion(reminder)
                            viewModel.dismissAlarm(reminder)
                            finish()
                        }
                    )
                }
            }
        }
    }
} 