package com.example.cursorreminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: Long,
    val medicationName: String,
    val dosage: String,
    val time: LocalDateTime,
    val scheduledDays: String,
    val isEnabled: Boolean,
    val completedDates: Set<LocalDate>,
)