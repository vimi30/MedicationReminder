package com.example.cursorreminder.data.repository

import com.example.cursorreminder.data.local.ReminderDao
import com.example.cursorreminder.data.mapper.toEntity
import com.example.cursorreminder.data.mapper.toReminder
import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
) : ReminderRepository {
    override fun getAllReminders(): Flow<List<Reminder>> {
        return dao.getAllReminders().map { entities ->
            entities.map { it.toReminder() }
        }
    }

    override suspend fun addReminder(reminder: Reminder) {
        dao.insertReminder(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) {
        dao.updateReminder(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        dao.deleteReminder(reminder.toEntity())
    }
} 