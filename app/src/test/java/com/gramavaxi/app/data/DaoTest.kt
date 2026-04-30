package com.gramavaxi.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.gramavaxi.app.data.db.AppDatabase
import com.gramavaxi.app.data.entity.Animal
import com.gramavaxi.app.data.entity.Camp
import com.gramavaxi.app.data.entity.DiseaseReport
import com.gramavaxi.app.data.entity.Vaccination
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() = db.close()

    @Test
    fun `insert and observe animal`() = runTest {
        val id = db.animalDao().insert(
            Animal(name = "Lakshmi", species = "Cow", breed = "Gir", ageMonths = 24,
                gender = "Female", photoPath = null, notes = null)
        )
        assertThat(id).isGreaterThan(0L)
        val all = db.animalDao().observeAll().first()
        assertThat(all).hasSize(1)
        assertThat(all[0].name).isEqualTo("Lakshmi")
    }

    @Test
    fun `cascading delete removes vaccinations`() = runTest {
        val animalId = db.animalDao().insert(
            Animal(name = "Bingo", species = "Goat", breed = "Local", ageMonths = 6,
                gender = "Male", photoPath = null, notes = null)
        )
        db.vaccinationDao().insertAll(
            listOf(
                Vaccination(animalId = animalId, vaccineName = "PPR", dueDate = 1000, cycleDays = 365),
                Vaccination(animalId = animalId, vaccineName = "FMD", dueDate = 2000, cycleDays = 180)
            )
        )
        assertThat(db.vaccinationDao().getAllForAnimal(animalId)).hasSize(2)

        val animal = db.animalDao().get(animalId)!!
        db.animalDao().delete(animal)
        assertThat(db.vaccinationDao().getAllForAnimal(animalId)).isEmpty()
    }

    @Test
    fun `getUpcoming returns only un-administered`() = runTest {
        val animalId = db.animalDao().insert(
            Animal(name = "X", species = "Goat", breed = "L", ageMonths = 1,
                gender = "Female", photoPath = null, notes = null)
        )
        db.vaccinationDao().insert(
            Vaccination(animalId = animalId, vaccineName = "Done", dueDate = 1000,
                administeredDate = 500, cycleDays = 90)
        )
        db.vaccinationDao().insert(
            Vaccination(animalId = animalId, vaccineName = "Pending", dueDate = 2000, cycleDays = 90)
        )
        val upcoming = db.vaccinationDao().getUpcoming()
        assertThat(upcoming).hasSize(1)
        assertThat(upcoming[0].vaccineName).isEqualTo("Pending")
    }

    @Test
    fun `camp upcoming filters by date`() = runTest {
        val now = System.currentTimeMillis()
        db.campDao().insert(Camp(title = "Past",   location = "L", date = now - 100_000, vaccinesOffered = "x"))
        db.campDao().insert(Camp(title = "Future", location = "L", date = now + 100_000, vaccinesOffered = "x"))
        val upcoming = db.campDao().upcoming(now)
        assertThat(upcoming.map { it.title }).containsExactly("Future")
    }

    @Test
    fun `disease report persisted`() = runTest {
        db.diseaseReportDao().insert(
            DiseaseReport(animalId = 1, animalName = "A", symptoms = "Fever",
                notes = null, photoPath = null, referenceId = "GV-XYZ")
        )
        val all = db.diseaseReportDao().observeAll().first()
        assertThat(all).hasSize(1)
        assertThat(all[0].referenceId).isEqualTo("GV-XYZ")
    }
}
