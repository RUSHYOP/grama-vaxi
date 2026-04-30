package com.gramavaxi.app.repo

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.gramavaxi.app.data.db.AppDatabase
import com.gramavaxi.app.data.entity.Animal
import com.gramavaxi.app.data.entity.Camp
import com.gramavaxi.app.notify.ReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: Repository
    private lateinit var wm: WorkManager

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            ctx,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        )
        wm = WorkManager.getInstance(ctx)
        repo = Repository(db, ReminderScheduler(ctx, wm))
    }

    @After fun tearDown() = db.close()

    private fun newGoat(name: String = "Bingo") = Animal(
        name = name, species = "Goat", breed = "Local", ageMonths = 6,
        gender = "Male", photoPath = null, notes = null
    )

    @Test
    fun `register animal generates full vaccine schedule`() = runTest {
        val id = repo.registerAnimal(newGoat())
        val schedule = repo.observeVaccinations(id).first()
        // Sheep/Goat schedule has 5 vaccines
        assertThat(schedule).hasSize(5)
        assertThat(schedule.map { it.vaccineName })
            .contains("PPR (Peste des Petits Ruminants)")
        // Every entry should be un-administered initially
        assertThat(schedule.all { it.administeredDate == null }).isTrue()
    }

    @Test
    fun `mark vaccine administered creates next cycle and cancels old reminders`() = runTest {
        val id = repo.registerAnimal(newGoat())
        val first = repo.observeVaccinations(id).first().first { it.vaccineName == "Deworming" }

        repo.markVaccineAdministered(first)

        val updated = repo.observeVaccinations(id).first()
        val administered = updated.filter { it.vaccineName == "Deworming" && it.administeredDate != null }
        val nextCycle = updated.filter { it.vaccineName == "Deworming" && it.administeredDate == null }
        assertThat(administered).hasSize(1)
        assertThat(nextCycle).hasSize(1)
        // The next cycle's due date must be ~cycleDays in the future
        val expected = administered[0].administeredDate!! + TimeUnit.DAYS.toMillis(first.cycleDays.toLong())
        assertThat(nextCycle[0].dueDate).isEqualTo(expected)
    }

    @Test
    fun `delete animal cancels all its vaccine reminders`() = runTest {
        val id = repo.registerAnimal(newGoat())
        val before = repo.observeVaccinations(id).first()
        assertThat(before).isNotEmpty()

        val animal = db.animalDao().get(id)!!
        repo.deleteAnimal(animal)

        // All previously scheduled vacc reminders must be cancelled
        before.forEach { v ->
            val three = wm.getWorkInfosForUniqueWork("vacc-3d-${v.id}").get()
            val due   = wm.getWorkInfosForUniqueWork("vacc-due-${v.id}").get()
            // Either CANCELLED state or empty (never enqueued because past)
            assertThat(three.all { it.state.isFinished }).isTrue()
            assertThat(due.all   { it.state.isFinished }).isTrue()
        }
        // DB cascade
        assertThat(db.vaccinationDao().getAllForAnimal(id)).isEmpty()
    }

    @Test
    fun `add camp schedules reminders and delete cancels them`() = runTest {
        val date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)
        val campId = repo.addCamp(Camp(title = "Temple Sq", location = "Village", date = date,
            vaccinesOffered = "PPR"))
        assertThat(wm.getWorkInfosForUniqueWork("camp-3d-$campId").get()).hasSize(1)

        val camp = db.campDao().get(campId)!!
        repo.deleteCamp(camp)
        assertThat(wm.getWorkInfosForUniqueWork("camp-3d-$campId").get()[0].state.isFinished).isTrue()
    }

    @Test
    fun `report disease produces unique reference id`() = runTest {
        val id = repo.registerAnimal(newGoat("Sheru"))
        val ref1 = repo.reportDisease(id, "Sheru", "Fever", null, null)
        val ref2 = repo.reportDisease(id, "Sheru", "Cough", null, null)
        assertThat(ref1).startsWith("GV-")
        assertThat(ref2).startsWith("GV-")
        assertThat(ref1).isNotEqualTo(ref2)
        assertThat(repo.observeReports().first()).hasSize(2)
    }

    @Test
    fun `rescheduleAllReminders re-enqueues without duplicating unique work`() = runTest {
        val id = repo.registerAnimal(newGoat())
        repo.addCamp(Camp(title = "C1", location = "L", date = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10),
            vaccinesOffered = "PPR"))
        val vaccsBefore = repo.observeVaccinations(id).first()

        repo.rescheduleAllReminders()

        // Unique work names mean one entry per (vacc-3d-id, vacc-due-id) — still exactly 1 each
        vaccsBefore.forEach { v ->
            assertThat(wm.getWorkInfosForUniqueWork("vacc-3d-${v.id}").get().size).isAtMost(1)
            assertThat(wm.getWorkInfosForUniqueWork("vacc-due-${v.id}").get().size).isAtMost(1)
        }
    }
}
