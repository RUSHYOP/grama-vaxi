package com.gramavaxi.app.repo

import android.content.Context
import com.gramavaxi.app.data.db.AppDatabase
import com.gramavaxi.app.data.entity.Animal
import com.gramavaxi.app.data.entity.Camp
import com.gramavaxi.app.data.entity.DiseaseReport
import com.gramavaxi.app.data.entity.Vaccination
import com.gramavaxi.app.notify.ReminderScheduler
import com.gramavaxi.app.schedule.VaccineSchedule
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

class Repository internal constructor(
    private val db: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val animalDao = db.animalDao()
    private val vaccDao = db.vaccinationDao()
    private val campDao = db.campDao()
    private val reportDao = db.diseaseReportDao()

    // ---------------- Animals ----------------
    fun observeAnimals(): Flow<List<Animal>> = animalDao.observeAll()
    fun observeAnimal(id: Long): Flow<Animal?> = animalDao.observe(id)
    suspend fun getAnimal(id: Long): Animal? = animalDao.get(id)

    suspend fun registerAnimal(animal: Animal): Long {
        val id = animalDao.insert(animal)
        val now = System.currentTimeMillis()
        val schedule = VaccineSchedule.forSpecies(animal.species).map { def ->
            Vaccination(
                animalId = id,
                vaccineName = def.name,
                dueDate = now + TimeUnit.DAYS.toMillis(def.firstShotAfterDays.toLong()),
                cycleDays = def.cycleDays
            )
        }
        val ids = vaccDao.insertAll(schedule)
        ids.forEachIndexed { idx, vid ->
            val v = schedule[idx].copy(id = vid)
            scheduler.scheduleVaccineReminder(v, animal.name)
        }
        return id
    }

    suspend fun updateAnimal(animal: Animal) = animalDao.update(animal)

    suspend fun deleteAnimal(animal: Animal) {
        vaccDao.getAllForAnimal(animal.id).forEach {
            scheduler.cancelVaccineReminders(it.id)
        }
        animalDao.delete(animal)
    }

    // ---------------- Vaccinations ----------------
    fun observeVaccinations(animalId: Long): Flow<List<Vaccination>> =
        vaccDao.observeForAnimal(animalId)

    fun observeUpcomingVaccinations(): Flow<List<Vaccination>> = vaccDao.observeUpcoming()

    suspend fun markVaccineAdministered(v: Vaccination) {
        val now = System.currentTimeMillis()
        // Cancel any pending reminders for this vaccine
        scheduler.cancelVaccineReminders(v.id)
        vaccDao.update(v.copy(administeredDate = now))
        // Schedule next cycle
        val animal = animalDao.get(v.animalId) ?: return
        val next = Vaccination(
            animalId = v.animalId,
            vaccineName = v.vaccineName,
            dueDate = now + TimeUnit.DAYS.toMillis(v.cycleDays.toLong()),
            cycleDays = v.cycleDays
        )
        val nextId = vaccDao.insert(next)
        scheduler.scheduleVaccineReminder(next.copy(id = nextId), animal.name)
    }

    suspend fun rescheduleAllReminders() {
        vaccDao.getUpcoming().forEach { v ->
            val animal = animalDao.get(v.animalId) ?: return@forEach
            scheduler.scheduleVaccineReminder(v, animal.name)
        }
        campDao.upcoming(System.currentTimeMillis()).forEach { c ->
            scheduler.scheduleCampReminder(c)
        }
    }

    // ---------------- Camps ----------------
    fun observeCamps(): Flow<List<Camp>> = campDao.observeAll()

    suspend fun addCamp(camp: Camp): Long {
        val id = campDao.insert(camp)
        scheduler.scheduleCampReminder(camp.copy(id = id))
        return id
    }

    suspend fun deleteCamp(camp: Camp) {
        scheduler.cancelCampReminders(camp.id)
        campDao.delete(camp)
    }

    // ---------------- Disease Reports ----------------
    fun observeReports(): Flow<List<DiseaseReport>> = reportDao.observeAll()

    suspend fun reportDisease(
        animalId: Long,
        animalName: String,
        symptoms: String,
        notes: String?,
        photoPath: String?
    ): String {
        val ref = "GV-" + UUID.randomUUID().toString().take(8).uppercase()
        reportDao.insert(
            DiseaseReport(
                animalId = animalId,
                animalName = animalName,
                symptoms = symptoms,
                notes = notes,
                photoPath = photoPath,
                referenceId = ref
            )
        )
        return ref
    }

    companion object {
        @Volatile private var INSTANCE: Repository? = null
        fun get(context: Context): Repository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Repository(
                AppDatabase.get(context.applicationContext),
                ReminderScheduler(context.applicationContext)
            ).also { INSTANCE = it }
        }
    }
}
