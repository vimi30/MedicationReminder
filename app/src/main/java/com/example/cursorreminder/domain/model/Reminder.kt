package com.example.cursorreminder.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

data class Reminder(
    val id: Long = System.currentTimeMillis(),
    val medicationName: String,
    val dosage: String = "",
    val time: LocalDateTime,
    val scheduledDays: List<DayOfWeek> = DayOfWeek.entries.toList(),
    val isEnabled: Boolean = true,
    val completedDates: Set<LocalDate> = emptySet(),
) 