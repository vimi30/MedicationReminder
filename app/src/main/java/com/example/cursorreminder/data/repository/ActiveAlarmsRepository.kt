package com.example.cursorreminder.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveAlarmsRepository @Inject constructor() {
    private val _activeAlarms = MutableStateFlow<Set<Long>>(emptySet())
    val activeAlarms: StateFlow<Set<Long>> = _activeAlarms.asStateFlow()

    fun setAlarmActive(reminderId: Long) {
        _activeAlarms.value += reminderId
    }

    fun setAlarmInactive(reminderId: Long) {
        _activeAlarms.value -= reminderId
    }
} 