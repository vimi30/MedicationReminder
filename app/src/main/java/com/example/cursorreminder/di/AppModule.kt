package com.example.cursorreminder.di

import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.example.cursorreminder.alarm.AlarmScheduler
import com.example.cursorreminder.data.local.ReminderDao
import com.example.cursorreminder.data.local.ReminderDatabase
import com.example.cursorreminder.data.repository.ReminderRepositoryImpl
import com.example.cursorreminder.domain.repository.ReminderRepository
import com.example.cursorreminder.domain.usecase.AddReminderUseCase
import com.example.cursorreminder.domain.usecase.GetRemindersUseCase
import com.example.cursorreminder.domain.usecase.UpdateReminderUseCase
import com.example.cursorreminder.permission.NotificationPermissionHandler
import com.example.cursorreminder.data.repository.ActiveAlarmsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context
    ): AlarmScheduler {
        return AlarmScheduler(context)
    }

    @Provides
    @Singleton
    fun provideNotificationPermissionHandler(
        @ApplicationContext context: Context
    ): NotificationPermissionHandler {
        return NotificationPermissionHandler(context)
    }

    @Provides
    @Singleton
    fun provideReminderDatabase(
        @ApplicationContext context: Context
    ): ReminderDatabase {
        return Room.databaseBuilder(
            context,
            ReminderDatabase::class.java,
            "reminders.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideReminderDao(database: ReminderDatabase): ReminderDao {
        return database.reminderDao
    }

    @Provides
    @Singleton
    fun provideReminderRepository(dao: ReminderDao): ReminderRepository {
        return ReminderRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideAddReminderUseCase(repository: ReminderRepository): AddReminderUseCase {
        return AddReminderUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetRemindersUseCase(repository: ReminderRepository): GetRemindersUseCase {
        return GetRemindersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateReminderUseCase(repository: ReminderRepository): UpdateReminderUseCase {
        return UpdateReminderUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideActiveAlarmsRepository(): ActiveAlarmsRepository {
        return ActiveAlarmsRepository()
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
} 