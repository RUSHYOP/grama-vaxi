package com.gramavaxi.app.data.dao

import androidx.room.*
import com.gramavaxi.app.data.entity.Camp
import kotlinx.coroutines.flow.Flow

@Dao
interface CampDao {
    @Query("SELECT * FROM camps ORDER BY date ASC")
    fun observeAll(): Flow<List<Camp>>

    @Query("SELECT * FROM camps WHERE date >= :now ORDER BY date ASC")
    suspend fun upcoming(now: Long): List<Camp>

    @Query("SELECT * FROM camps WHERE id = :id")
    suspend fun get(id: Long): Camp?

    @Insert
    suspend fun insert(c: Camp): Long

    @Update
    suspend fun update(c: Camp)

    @Delete
    suspend fun delete(c: Camp)
}
