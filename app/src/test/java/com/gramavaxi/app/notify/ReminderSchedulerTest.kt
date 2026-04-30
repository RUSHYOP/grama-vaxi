package com.gramavaxi.app.notify

import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.gramavaxi.app.data.entity.Camp
import com.gramavaxi.app.data.entity.Vaccination
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ReminderSchedulerTest {

    private lateinit var wm: WorkManager
    private lateinit var scheduler: ReminderScheduler

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(ctx, config)
        wm = WorkManager.getInstance(ctx)
        scheduler = ReminderScheduler(ctx, wm)
    }

    @Test
    fun `schedules both 3-day and due-date workers for a future vaccine`() {
        val due = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)
        val v = Vaccination(id = 42, animalId = 1, vaccineName = "PPR", dueDate = due, cycleDays = 365)

        scheduler.scheduleVaccineReminder(v, "Lakshmi")

        val three = wm.getWorkInfosForUniqueWork("vacc-3d-42").get()
        val due0 = wm.getWorkInfosForUniqueWork("vacc-due-42").get()
        assertThat(three).hasSize(1)
        assertThat(due0).hasSize(1)
        assertThat(three[0].state).isEqualTo(WorkInfo.State.ENQUEUED)
        assertThat(due0[0].state).isEqualTo(WorkInfo.State.ENQUEUED)
    }

    @Test
    fun `does not schedule past-due vaccines`() {
        val due = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val v = Vaccination(id = 99, animalId = 1, vaccineName = "X", dueDate = due, cycleDays = 90)

        scheduler.scheduleVaccineReminder(v, "Past")

        val three = wm.getWorkInfosForUniqueWork("vacc-3d-99").get()
        val dueWork = wm.getWorkInfosForUniqueWork("vacc-due-99").get()
        assertThat(three).isEmpty()
        assertThat(dueWork).isEmpty()
    }

    @Test
    fun `cancels both vaccine reminders`() {
        val due = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15)
        val v = Vaccination(id = 7, animalId = 1, vaccineName = "FMD", dueDate = due, cycleDays = 180)
        scheduler.scheduleVaccineReminder(v, "G")
        scheduler.cancelVaccineReminders(7)

        val three = wm.getWorkInfosForUniqueWork("vacc-3d-7").get()
        val dueWork = wm.getWorkInfosForUniqueWork("vacc-due-7").get()
        // After cancellation the work-info state must be CANCELLED
        assertThat(three[0].state).isEqualTo(WorkInfo.State.CANCELLED)
        assertThat(dueWork[0].state).isEqualTo(WorkInfo.State.CANCELLED)
    }

    @Test
    fun `schedules camp 3-day and 1-day reminders`() {
        val date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)
        val c = Camp(id = 5, title = "Temple", location = "Square", date = date, vaccinesOffered = "PPR, FMD")

        scheduler.scheduleCampReminder(c)

        assertThat(wm.getWorkInfosForUniqueWork("camp-3d-5").get()).hasSize(1)
        assertThat(wm.getWorkInfosForUniqueWork("camp-1d-5").get()).hasSize(1)
    }

    @Test
    fun `cancels camp reminders`() {
        val date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)
        val c = Camp(id = 3, title = "T", location = "L", date = date, vaccinesOffered = "x")
        scheduler.scheduleCampReminder(c)
        scheduler.cancelCampReminders(3)

        assertThat(wm.getWorkInfosForUniqueWork("camp-3d-3").get()[0].state)
            .isEqualTo(WorkInfo.State.CANCELLED)
        assertThat(wm.getWorkInfosForUniqueWork("camp-1d-3").get()[0].state)
            .isEqualTo(WorkInfo.State.CANCELLED)
    }

    @Test
    fun `re-schedule replaces existing work`() {
        val due1 = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(20)
        val v1 = Vaccination(id = 11, animalId = 1, vaccineName = "PPR", dueDate = due1, cycleDays = 365)
        scheduler.scheduleVaccineReminder(v1, "A")

        val due2 = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(40)
        val v2 = v1.copy(dueDate = due2)
        scheduler.scheduleVaccineReminder(v2, "A")

        // Unique work policy REPLACE → still exactly one work per tag
        assertThat(wm.getWorkInfosForUniqueWork("vacc-3d-11").get()).hasSize(1)
        assertThat(wm.getWorkInfosForUniqueWork("vacc-due-11").get()).hasSize(1)
    }
}
