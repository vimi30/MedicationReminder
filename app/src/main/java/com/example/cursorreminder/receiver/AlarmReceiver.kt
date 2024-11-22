package com.example.cursorreminder.receiver

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import com.example.cursorreminder.AlarmActivity
import com.example.cursorreminder.R
import com.example.cursorreminder.data.repository.ActiveAlarmsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS = "com.example.cursorreminder.DISMISS_ALARM"
        const val ACTION_ALARM_STARTED = "com.example.cursorreminder.ALARM_STARTED"
        const val ACTION_ALARM_DISMISSED = "com.example.cursorreminder.ALARM_DISMISSED"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"

        // Make MediaPlayer static
        @Volatile
        private var mediaPlayer: MediaPlayer? = null
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var activeAlarmsRepository: ActiveAlarmsRepository


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        when (intent?.action) {
            ACTION_DISMISS -> {
                val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
                if (reminderId != -1L) {
                    // Stop and release MediaPlayer
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null

                    // Cancel notification
                    notificationManager.cancel(reminderId.toInt())

                    // Update active alarms
                    activeAlarmsRepository.setAlarmInactive(reminderId)
                }
            }

            else -> {
                val reminderId = intent?.getLongExtra(EXTRA_REMINDER_ID, -1L) ?: -1L
                val medicationName = intent?.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"


                // Create and start MediaPlayer
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, R.raw.simple_notification).apply {
                    isLooping = true
                    start()
                }


                // Set alarm as active
                activeAlarmsRepository.setAlarmActive(reminderId)

                // Create notification
                val notification = createNotification(context, medicationName, reminderId)
                notificationManager.notify(reminderId.toInt(), notification)

                if (!isAppInForeground(context = context)) {
                    // Start AlarmActivity
                    val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_REMINDER_ID, reminderId)
                        putExtra(EXTRA_MEDICATION_NAME, medicationName)
                    }
                    context.startActivity(alarmIntent)
                }
            }
        }
    }

    private fun createNotification(
        context: Context,
        medicationName: String,
        reminderId: Long,
    ): Notification {
        val channelId = createNotificationChannel(context)

        val intent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_MEDICATION_NAME, medicationName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Medication Reminder")
            .setContentText("Time to take $medicationName")
            .setSmallIcon(R.drawable.icon_medication)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel(context: Context): String {
        val channelId = "medication_reminder_channel"
        val channel = NotificationChannel(
            channelId,
            "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for medication reminders"
            enableLights(true)
            enableVibration(true)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName
            ) {
                return true
            }
        }
        return false
    }
}