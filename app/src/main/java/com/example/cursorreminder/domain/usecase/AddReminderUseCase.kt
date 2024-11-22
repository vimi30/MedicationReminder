package com.example.cursorreminder.domain.usecase

import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.domain.repository.ReminderRepository
import javax.inject.Inject

class AddReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder) {
        repository.addReminder(reminder)
    }
} 