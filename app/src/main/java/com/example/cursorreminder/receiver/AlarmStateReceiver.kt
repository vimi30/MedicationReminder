package com.example.cursorreminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cursorreminder.data.repository.ActiveAlarmsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmStateReceiver : BroadcastReceiver() {
    @Inject
    lateinit var activeAlarmsRepository: ActiveAlarmsRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            AlarmReceiver.ACTION_ALARM_STARTED -> {
                val reminderId = intent.getLongExtra(AlarmReceiver.EXTRA_REMINDER_ID, -1)

                if (reminderId != -1L) {
                    activeAlarmsRepository.setAlarmActive(reminderId)
                }
            }

            AlarmReceiver.ACTION_ALARM_DISMISSED -> {
                val reminderId = intent.getLongExtra(AlarmReceiver.EXTRA_REMINDER_ID, -1)
                if (reminderId != -1L) {
                    activeAlarmsRepository.setAlarmInactive(reminderId)
                }
            }
        }
    }
} 