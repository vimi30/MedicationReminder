package com.example.cursorreminder.util

import com.example.cursorreminder.domain.model.Reminder
import java.time.DayOfWeek
import java.time.LocalDateTime

object TestData {
    fun createTestReminder() = Reminder(
        id = 1L,
        medicationName = "Test Med",
        dosage = "1 pill",
        time = LocalDateTime.now(),
        scheduledDays = listOf(DayOfWeek.MONDAY),
        completedDates = setOf()
    )
}