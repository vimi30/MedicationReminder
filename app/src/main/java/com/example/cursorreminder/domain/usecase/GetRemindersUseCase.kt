package com.example.cursorreminder.domain.usecase

import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(): Flow<List<Reminder>> {
        return repository.getAllReminders()
    }
} 