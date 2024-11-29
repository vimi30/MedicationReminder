package com.example.cursorreminder.presentation

import android.app.Application
import com.example.cursorreminder.alarm.AlarmScheduler
import com.example.cursorreminder.data.repository.ActiveAlarmsRepository
import com.example.cursorreminder.domain.model.Reminder
import com.example.cursorreminder.domain.usecase.AddReminderUseCase
import com.example.cursorreminder.domain.usecase.DeleteReminderUseCase
import com.example.cursorreminder.domain.usecase.GetRemindersUseCase
import com.example.cursorreminder.domain.usecase.UpdateReminderUseCase
import com.example.cursorreminder.permission.NotificationPermissionHandler
import com.example.cursorreminder.presentation.viewmodel.ReminderViewModel
import com.example.cursorreminder.presentation.viewmodel.ScheduleType
import com.example.cursorreminder.util.MainDispatcherRule
import com.example.cursorreminder.util.TestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class ReminderViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ReminderViewModel
    private lateinit var getReminderUseCase: GetRemindersUseCase
    private lateinit var addReminderUseCase: AddReminderUseCase
    private lateinit var updateReminderUseCase: UpdateReminderUseCase
    private lateinit var deleteReminderUseCase: DeleteReminderUseCase
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var activeAlarmsRepository: ActiveAlarmsRepository
    private lateinit var notificationPermissionHandler: NotificationPermissionHandler

    @Before
    fun setup() {
        getReminderUseCase = mockk(relaxed = true)
        addReminderUseCase = mockk(relaxed = true)
        updateReminderUseCase = mockk(relaxed = true)
        deleteReminderUseCase = mockk(relaxed = true)
        alarmScheduler = mockk(relaxed = true)
        activeAlarmsRepository = mockk {
            every { activeAlarms } returns MutableStateFlow(emptySet())
        }
        notificationPermissionHandler = mockk(relaxed = true)
        // Mock application context for Intent creation
        val mockContext = mockk<Application>(relaxed = true)

//        // Mock Intent
//        mockkStatic(Intent::class)
//        every { Intent().setAction(any()) } returns mockk(relaxed = true)
//        every { Intent(any(), any<Class<*>>()) } returns mockk(relaxed = true)

        viewModel = ReminderViewModel(
            getRemindersUseCase = getReminderUseCase,
            addReminderUseCase = addReminderUseCase,
            updateReminderUseCase = updateReminderUseCase,
            deleteReminderUseCase = deleteReminderUseCase,
            alarmScheduler = alarmScheduler,
            activeAlarmsRepository = activeAlarmsRepository,
            application = mockContext,  // Use the mocked context,
            notificationPermissionHandler = notificationPermissionHandler
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `adding reminder with valid data should succeed`() = runTest {
        // Given
        val selectedTime = LocalDateTime.now()

        // When
        viewModel.updateMedicationName("Vitamin C")
        viewModel.updateDosage("1 pill")
        viewModel.updateSelectedTime(selectedTime)
        viewModel.toggleDay(DayOfWeek.MONDAY)

        viewModel.addReminder()

        // Advance coroutines
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            addReminderUseCase.invoke(any())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggling reminder completion should update completion status`() = runTest {
        // Given
        val reminder = TestData.createTestReminder().copy(completedDates = setOf())
        val activeAlarmsFlow = MutableStateFlow(setOf(reminder.id))  // Add this line

        // Mock the activeAlarms flow
        every { activeAlarmsRepository.activeAlarms } returns activeAlarmsFlow
        coEvery { updateReminderUseCase.invoke(any()) } returns Unit

        // When
        viewModel.toggleCompletion(reminder)

        // Advance coroutines
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            updateReminderUseCase.invoke(withArg { updatedReminder ->
                assert(updatedReminder.id == reminder.id)
                assert(updatedReminder.completedDates.contains(LocalDate.now()))
            })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleting reminder should remove from repository and cancel alarm`() = runTest {
        // Given
        val reminder = TestData.createTestReminder()
        coEvery { deleteReminderUseCase(any()) } just Runs

        // When
        viewModel.deleteReminder(reminder)
        // Advance coroutines
        advanceUntilIdle()

        // Then
        coVerify { deleteReminderUseCase(reminder) }
        verify { alarmScheduler.cancel(reminder) }
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun `dismissing alarm should reschedule for next occurrence`() = runTest {
//        // Given
//        val reminder = TestData.createTestReminder()
//        val activeAlarmsFlow = MutableStateFlow(setOf(reminder.id))
//
//        // Mock repositories and scheduler
//        every { activeAlarmsRepository.activeAlarms } returns activeAlarmsFlow
//        coEvery { activeAlarmsRepository.setAlarmInactive(any()) } just Runs
//        coEvery { alarmScheduler.rescheduleAfterAlarm(any()) } just Runs
//
//        // Mock Android Intent
//        mockkStatic(Intent::class)
//        every { Intent().setAction(any()) } returns mockk(relaxed = true)
//        every { Intent(any(), any<Class<*>>()) } returns mockk(relaxed = true)
//
//        // When
//        viewModel.dismissAlarm(reminder)
//        advanceUntilIdle()
//
//        // Then
//        coVerify {
//            activeAlarmsRepository.setAlarmInactive(reminder.id)
//            alarmScheduler.rescheduleAfterAlarm(reminder)
//        }
//    }

    @Test
    fun `toggling day should update selected days`() = runTest {
        // When
        viewModel.toggleDay(DayOfWeek.MONDAY)
        viewModel.toggleDay(DayOfWeek.WEDNESDAY)

        // Then
        val selectedDays = viewModel.selectedDays.value
        assert(selectedDays.contains(DayOfWeek.MONDAY))
        assert(selectedDays.contains(DayOfWeek.WEDNESDAY))

        // When toggling off
        viewModel.toggleDay(DayOfWeek.MONDAY)

        // Then
        assert(!viewModel.selectedDays.value.contains(DayOfWeek.MONDAY))
        assert(viewModel.selectedDays.value.contains(DayOfWeek.WEDNESDAY))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `today reminders should filter correctly`() = runTest {
        // Given
        val today = LocalDate.now()
        val todayDayOfWeek = today.dayOfWeek

        // Create test data
        val todayReminder = TestData.createTestReminder().copy(
            id = 1L,
            scheduledDays = listOf(todayDayOfWeek),
            isEnabled = true
        )

        val tomorrowReminder = TestData.createTestReminder().copy(
            id = 2L,
            scheduledDays = listOf(todayDayOfWeek.plus(1)),
            isEnabled = true
        )

        // Set up initial flow with just today's reminder
        val remindersFlow = MutableStateFlow(listOf(todayReminder, tomorrowReminder))

        // Set up getReminderUseCase mock BEFORE creating viewModel
        every { getReminderUseCase() } returns remindersFlow

        // Create new ViewModel instance
        val testViewModel = ReminderViewModel(
            getRemindersUseCase = getReminderUseCase,
            addReminderUseCase = addReminderUseCase,
            updateReminderUseCase = updateReminderUseCase,
            deleteReminderUseCase = deleteReminderUseCase,
            alarmScheduler = alarmScheduler,
            activeAlarmsRepository = activeAlarmsRepository,
            application = mockk(relaxed = true),
            notificationPermissionHandler = notificationPermissionHandler
        )

        // Debug prints for initial state
        println("Initial reminders flow value: ${remindersFlow.value}")
        println("Initial today reminders value: ${testViewModel.todayReminders.value}")

        // Collect values from both flows to debug
        val remindersCollected = mutableListOf<List<Reminder>>()
        val todayRemindersCollected = mutableListOf<List<Reminder>>()

        val collectJob = launch {
            launch {
                testViewModel.reminders.collect {
                    remindersCollected.add(it)
                    println("Collected reminders: $it")
                }
            }
            launch {
                testViewModel.todayReminders.collect {
                    todayRemindersCollected.add(it)
                    println("Collected today reminders: $it")
                }
            }
        }

        // Advance coroutines
        advanceUntilIdle()

        // Debug prints for final state
        println("Final reminders collected: $remindersCollected")
        println("Final today reminders collected: $todayRemindersCollected")
        println("Final reminders flow value: ${remindersFlow.value}")
        println("Final today reminders value: ${testViewModel.todayReminders.value}")

        // Then
        assertTrue(testViewModel.todayReminders.value.isNotEmpty())
        assertTrue(testViewModel.todayReminders.value.contains(todayReminder))
        assertFalse(testViewModel.todayReminders.value.contains(tomorrowReminder))

        // Cleanup
        collectJob.cancel()

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `adding reminder with empty medication name should not proceed`() = runTest {
        // Given
        coEvery { addReminderUseCase(any()) } just Runs

        // When
        viewModel.updateMedicationName("")
        viewModel.updateDosage("1 pill")
        viewModel.toggleDay(DayOfWeek.MONDAY)
        viewModel.addReminder()

        // Advance coroutines
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { addReminderUseCase(any()) }
        verify(exactly = 0) { alarmScheduler.schedule(any()) }
    }

    @Test
    fun `adding reminder with no selected days should not proceed`() = runTest {
        // Given
        coEvery { addReminderUseCase(any()) } just Runs

        // When
        viewModel.updateMedicationName("Test Med")
        viewModel.updateDosage("1 pill")
        viewModel.addReminder()

        // Then
        coVerify(exactly = 0) { addReminderUseCase(any()) }
    }

//    @Test
//    fun `updating reminder that fails should handle error`() = runTest {
//        // Given
//        val reminder = TestData.createTestReminder()
//        coEvery { updateReminderUseCase(any()) } throws Exception("Database error")
//
//        // When
//        viewModel.toggleCompletion(reminder)
//
//        // Then
//        // Verify error state is updated (if you implement error handling)
//         assert(viewModel.errorMessage.value != null)
//    }

//    @Test
//    fun `scheduling alarm that fails should handle error`() = runTest {
//        // Given
//        coEvery { addReminderUseCase(any()) } just Runs
//        every { alarmScheduler.schedule(any()) } throws Exception("Alarm scheduling failed")
//
//        // When
//        viewModel.updateMedicationName("Test Med")
//        viewModel.updateDosage("1 pill")
//        viewModel.toggleDay(DayOfWeek.MONDAY)
//        viewModel.addReminder()
//
//        // Then
//        // Verify error handling and user notification
//    }

    // Additional ViewModel Tests
    @Test
    fun `updating selected time should update state`() = runTest {
        // Given
        val newTime = LocalDateTime.now().plusHours(1)

        // When
        viewModel.updateSelectedTime(newTime)

        // Then
        assertEquals(newTime, viewModel.selectedTime.value)
    }

    @Test
    fun `updating schedule type should clear selected days for daily schedule`() = runTest {
        // Given
        viewModel.toggleDay(DayOfWeek.MONDAY)
        viewModel.toggleDay(DayOfWeek.WEDNESDAY)

        // When
        viewModel.updateScheduleType(ScheduleType.DAILY)

        // Then
        assertTrue(viewModel.selectedDays.value.containsAll(DayOfWeek.entries))
    }

    @Test
    fun `updating schedule type to custom should maintain selected days`() = runTest {
        // Given
        viewModel.updateScheduleType(ScheduleType.DAILY)

        // When
        viewModel.updateScheduleType(ScheduleType.CUSTOM)
        viewModel.toggleDay(DayOfWeek.MONDAY)

        // Then
        assertEquals(setOf(DayOfWeek.MONDAY), viewModel.selectedDays.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `completed dates should update correctly`() = runTest {
//        // Given
//        val reminder = TestData.createTestReminder()
//        val today = LocalDate.now()
//        coEvery { updateReminderUseCase(any()) } just Runs
//
//        // When
//        viewModel.toggleCompletion(reminder)
//
//        // Then
//        assertTrue(viewModel.completedDates.value.contains(today))

        // Given
        val today = LocalDate.now()
        val reminder = TestData.createTestReminder()
        val remindersFlow = MutableStateFlow(listOf(reminder))

        // Set up mocks
        every { getReminderUseCase() } returns remindersFlow
        coEvery { updateReminderUseCase(any()) } answers {
            val updatedReminder = firstArg<Reminder>()
            // Update the flow with the new reminder
            remindersFlow.value = listOf(updatedReminder)
        }

        // Create new ViewModel instance
        val testViewModel = ReminderViewModel(
            getRemindersUseCase = getReminderUseCase,
            addReminderUseCase = addReminderUseCase,
            updateReminderUseCase = updateReminderUseCase,
            deleteReminderUseCase = deleteReminderUseCase,
            alarmScheduler = alarmScheduler,
            activeAlarmsRepository = activeAlarmsRepository,
            application = mockk(relaxed = true),
            notificationPermissionHandler = notificationPermissionHandler
        )

        // Collect completedDates to ensure updates are captured
        val completedDatesCollected = mutableListOf<Set<LocalDate>>()
        val job = launch {
            testViewModel.completedDates.collect {
                completedDatesCollected.add(it)
                println("Collected completed dates: $it")
            }
        }

        // When
        testViewModel.toggleCompletion(reminder)
        advanceUntilIdle()

        // Debug prints
        println("Initial reminders: ${remindersFlow.value}")
        println("Updated reminders: ${remindersFlow.value}")
        println("All collected completed dates: $completedDatesCollected")
        println("Final completed dates: ${testViewModel.completedDates.value}")

        // Then
        assertTrue(testViewModel.completedDates.value.isNotEmpty())
        assertTrue(testViewModel.completedDates.value.contains(today))

        // Cleanup
        job.cancel()
    }
//
//    @Test
//    fun `active alarms should update when alarm is triggered`() = runTest {
//        // Given
//        val reminder = TestData.createTestReminder()
//
//        // When
////        viewModel.setAlarmActive(reminder.id)
//
//        // Then
//        assertTrue(viewModel.activeAlarms.value.contains(reminder.id))
//    }

    @Test
    fun `getNextOccurrence should calculate correct next date`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val monday = DayOfWeek.MONDAY

        // When
        val nextOccurrence = viewModel.getNextOccurrence(now, monday)

        // Then
        assertEquals(monday, nextOccurrence.dayOfWeek)
        assertTrue(nextOccurrence.isAfter(now) || nextOccurrence.isEqual(now))
    }
//
//    @Test
//    fun `dismissing non existent alarm should be handled gracefully`() = runTest {
//        // Given
//        val nonExistentReminder = TestData.createTestReminder().copy(id = -1)
//
//        // When & Then
//        assertDoesNotThrow { viewModel.dismissAlarm(nonExistentReminder) }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggling completion for already completed reminder should remove completion`() = runTest {
// Given
        val today = LocalDate.now()
        val reminder = TestData.createTestReminder()
        val remindersFlow = MutableStateFlow(listOf(reminder))

        // Set up mocks
        every { getReminderUseCase() } returns remindersFlow
        coEvery { updateReminderUseCase(any()) } answers {
            val updatedReminder = firstArg<Reminder>()
            remindersFlow.value = listOf(updatedReminder)
            println("Reminder updated with completed dates: ${updatedReminder.completedDates}")
        }

        // Create new ViewModel instance
        val testViewModel = ReminderViewModel(
            getRemindersUseCase = getReminderUseCase,
            addReminderUseCase = addReminderUseCase,
            updateReminderUseCase = updateReminderUseCase,
            deleteReminderUseCase = deleteReminderUseCase,
            alarmScheduler = alarmScheduler,
            activeAlarmsRepository = activeAlarmsRepository,
            application = mockk(relaxed = true),
            notificationPermissionHandler = notificationPermissionHandler
        )

        // Collect completedDates to ensure updates are captured
        val completedDatesCollected = mutableListOf<Set<LocalDate>>()
        val job = launch {
            testViewModel.completedDates.collect {
                completedDatesCollected.add(it)
                println("Collected completed dates: $it")
            }
        }

        // When
        println("Initial state - Completed dates: ${testViewModel.completedDates.value}")

        testViewModel.toggleCompletion(reminder) // Complete
        advanceUntilIdle()
        println("After first toggle - Completed dates: ${testViewModel.completedDates.value}")

        testViewModel.toggleCompletion(reminder.copy(completedDates = setOf(today))) // Uncomplete
        advanceUntilIdle()
        println("After second toggle - Completed dates: ${testViewModel.completedDates.value}")

        // Then
        coVerify(exactly = 2) { updateReminderUseCase(any()) }
        assertTrue(testViewModel.completedDates.value.isEmpty())

        // Debug final state
        println("Final reminders state: ${remindersFlow.value}")
        println("All collected completed dates: $completedDatesCollected")

        // Cleanup
        job.cancel()
    }
}