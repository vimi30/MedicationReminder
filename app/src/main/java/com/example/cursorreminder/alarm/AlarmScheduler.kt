package com.example.cursorreminder.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.receiver.AlarmReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(reminder: Reminder) {
        // Calculate next alarm time based on scheduled days
        val nextAlarmTime = getNextAlarmTime(reminder)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_STARTED
            putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_NAME, reminder.medicationName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                nextAlarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                pendingIntent
            ),
            pendingIntent
        )
    }

    private fun getNextAlarmTime(reminder: Reminder): LocalDateTime {
        val now = LocalDateTime.now()
        val reminderTime = LocalTime.of(reminder.time.hour, reminder.time.minute)

        // If no days are scheduled, return the original time
        if (reminder.scheduledDays.isEmpty()) {
            return reminder.time
        }

        // Find the next scheduled day
        var nextDate = now.toLocalDate()
        var daysChecked = 0

        while (daysChecked < 7) {
            if (reminder.scheduledDays.contains(nextDate.dayOfWeek)) {
                val nextDateTime = LocalDateTime.of(nextDate, reminderTime)
                if (nextDateTime.isAfter(now)) {
                    return nextDateTime
                }
            }
            nextDate = nextDate.plusDays(1)
            daysChecked++
        }

        // If no valid time found in the next 7 days, schedule for the first available day next week
        nextDate = now.toLocalDate().plusDays(7)
        while (!reminder.scheduledDays.contains(nextDate.dayOfWeek)) {
            nextDate = nextDate.plusDays(1)
        }
        return LocalDateTime.of(nextDate, reminderTime)
    }

    fun rescheduleAfterAlarm(reminder: Reminder) {
        // Cancel existing alarm
        cancel(reminder)

        // Schedule next alarm based on scheduled days
        if (reminder.isEnabled) {
            val nextReminder = reminder.copy(
                time = getNextAlarmTime(reminder)
            )
            schedule(nextReminder)
        }
    }

    fun cancel(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
} 