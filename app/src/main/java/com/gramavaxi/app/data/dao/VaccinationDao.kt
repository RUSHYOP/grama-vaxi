package com.gramavaxi.app.data.dao

import androidx.room.*
import com.gramavaxi.app.data.entity.Vaccination
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccinationDao {
    @Query("SELECT * FROM vaccinations WHERE animalId = :animalId ORDER BY dueDate ASC")
    fun observeForAnimal(animalId: Long): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE administeredDate IS NULL ORDER BY dueDate ASC")
    fun observeUpcoming(): Flow<List<Vaccination>>

    @Query("SELECT * FROM vaccinations WHERE administeredDate IS NULL")
    suspend fun getUpcoming(): List<Vaccination>

    @Query("SELECT * FROM vaccinations WHERE animalId = :animalId")
    suspend fun getAllForAnimal(animalId: Long): List<Vaccination>

    @Query("SELECT * FROM vaccinations WHERE id = :id")
    suspend fun get(id: Long): Vaccination?

    @Insert
    suspend fun insert(v: Vaccination): Long

    @Insert
    suspend fun insertAll(vs: List<Vaccination>): List<Long>

    @Update
    suspend fun update(v: Vaccination)

    @Delete
    suspend fun delete(v: Vaccination)
}
