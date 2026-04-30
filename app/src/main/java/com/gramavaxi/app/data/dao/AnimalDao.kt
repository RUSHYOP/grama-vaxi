package com.gramavaxi.app.data.dao

import androidx.room.*
import com.gramavaxi.app.data.entity.Animal
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animals ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Animal>>

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun get(id: Long): Animal?

    @Query("SELECT * FROM animals WHERE id = :id")
    fun observe(id: Long): Flow<Animal?>

    @Insert
    suspend fun insert(animal: Animal): Long

    @Update
    suspend fun update(animal: Animal)

    @Delete
    suspend fun delete(animal: Animal)
}
