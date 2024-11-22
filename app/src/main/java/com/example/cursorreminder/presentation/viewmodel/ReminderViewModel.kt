package com.example.cursorreminder.presentation.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursorreminder.alarm.AlarmScheduler
import com.example.cursorreminder.data.repository.ActiveAlarmsRepository
import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.domain.usecase.AddReminderUseCase
import com.example.cursorreminder.domain.usecase.DeleteReminderUseCase
import com.example.cursorreminder.domain.usecase.GetRemindersUseCase
import com.example.cursorreminder.domain.usecase.UpdateReminderUseCase
import com.example.cursorreminder.permission.NotificationPermissionHandler
import com.example.cursorreminder.receiver.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val application: Application,
    private val addReminderUseCase: AddReminderUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val notificationPermissionHandler: NotificationPermissionHandler,
    private val alarmScheduler: AlarmScheduler,
    private val activeAlarmsRepository: ActiveAlarmsRepository,
) : ViewModel() {

    private val _medicationName = MutableStateFlow("")
    val medicationName: StateFlow<String> = _medicationName.asStateFlow()

    private val _selectedTime = MutableStateFlow<LocalDateTime>(LocalDateTime.now())
    val selectedTime: StateFlow<LocalDateTime> = _selectedTime.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _scheduleType = MutableStateFlow(ScheduleType.DAILY)
    val scheduleType = _scheduleType.asStateFlow()

    private val _selectedDays = MutableStateFlow(emptySet<DayOfWeek>())
    val selectedDays = _selectedDays.asStateFlow()

    private val _dosage = MutableStateFlow("")
    val dosage: StateFlow<String> = _dosage.asStateFlow()

    val reminders: StateFlow<List<Reminder>> = getRemindersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val completedDates: StateFlow<Set<LocalDate>> = reminders.map { reminderList ->
        reminderList.flatMap { it.completedDates }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptySet())

    val activeAlarms = activeAlarmsRepository.activeAlarms

    init {
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        _hasNotificationPermission.value = notificationPermissionHandler.hasNotificationPermission()
    }

    fun updateDosage(dosage: String) {
        _dosage.value = dosage
    }

    fun updateScheduleType(type: ScheduleType) {
        _scheduleType.value = type
        if (type == ScheduleType.DAILY) {
            _selectedDays.value = DayOfWeek.entries.toSet()
        } else {
            _selectedDays.value = emptySet()
        }
    }

    fun toggleDay(day: DayOfWeek) {
        _selectedDays.value = _selectedDays.value.toMutableSet().apply {
            if (contains(day)) remove(day) else add(day)
        }
    }

    fun updateMedicationName(name: String) {
        _medicationName.value = name
    }

    fun updateSelectedTime(time: LocalDateTime) {
        _selectedTime.value = time
    }

    fun addReminder() {
        viewModelScope.launch {
            val reminder = Reminder(
                medicationName = medicationName.value,
                dosage = dosage.value,
                time = selectedTime.value,
                scheduledDays = selectedDays.value.toList(),
                isEnabled = true
            )

            addReminderUseCase(reminder)

            // Schedule alarms for each selected day
            selectedDays.value.forEach { day ->
                val nextOccurrence = getNextOccurrence(selectedTime.value, day)
                alarmScheduler.schedule(reminder.copy(time = nextOccurrence))
            }

            // Reset form
            _medicationName.value = ""
            _selectedDays.value = emptySet()
            _scheduleType.value = ScheduleType.DAILY
        }
    }

    private fun getNextOccurrence(time: LocalDateTime, dayOfWeek: DayOfWeek): LocalDateTime {
        var nextTime = time
        while (nextTime.dayOfWeek != dayOfWeek) {
            nextTime = nextTime.plusDays(1)
        }
        return nextTime
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(isEnabled = !reminder.isEnabled)
            updateReminderUseCase(updatedReminder)

            if (updatedReminder.isEnabled) {
                alarmScheduler.schedule(updatedReminder)
            } else {
                alarmScheduler.cancel(updatedReminder)
            }
        }
    }

    fun dismissAlarm(reminder: Reminder) {

        viewModelScope.launch {
            // Send broadcast to stop sound
            Intent(application, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_DISMISS
                putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
                application.sendBroadcast(this)
            }

            // Update active alarms
            activeAlarmsRepository.setAlarmInactive(reminder.id)
        }
    }

    fun toggleCompletion(reminder: Reminder, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val updatedDates = if (reminder.completedDates.contains(date)) {
                reminder.completedDates - date
            } else {
                // If marking as taken, dismiss any active alarm
                if (activeAlarms.value.contains(reminder.id)) {
                    dismissAlarm(reminder)
                }
                reminder.completedDates + date
            }

            val updatedReminder = reminder.copy(completedDates = updatedDates)
            updateReminderUseCase(updatedReminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            alarmScheduler.cancel(reminder)
            deleteReminderUseCase(reminder)
        }
    }
}

enum class ScheduleType {
    DAILY, CUSTOM
}