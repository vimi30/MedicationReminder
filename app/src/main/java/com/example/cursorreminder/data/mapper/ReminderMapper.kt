package com.example.cursorreminder.data.mapper

import com.example.cursorreminder.data.local.ReminderEntity
import com.example.cursorreminder.domain.model.Reminder
import java.time.DayOfWeek

fun ReminderEntity.toReminder(): Reminder {
    return Reminder(
        id = id,
        medicationName = medicationName,
        dosage = dosage,
        time = time,
        scheduledDays = scheduledDays.takeIf { it.isNotEmpty() }
            ?.split(",")
            ?.mapNotNull { dayString ->
                try {
                    DayOfWeek.of(dayString.toInt())
                } catch (e: Exception) {
                    null
                }
            }
            ?: DayOfWeek.entries, // Default to all days if empty
        isEnabled = isEnabled,
        completedDates = completedDates
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        medicationName = medicationName,
        dosage = dosage,
        time = time,
        scheduledDays = scheduledDays.joinToString(","){it.value.toString()},
        isEnabled = isEnabled,
        completedDates = completedDates
    )
}
