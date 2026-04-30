package com.gramavaxi.app.data.dao

import androidx.room.*
import com.gramavaxi.app.data.entity.DiseaseReport
import kotlinx.coroutines.flow.Flow

@Dao
interface DiseaseReportDao {
    @Query("SELECT * FROM disease_reports ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DiseaseReport>>

    @Insert
    suspend fun insert(r: DiseaseReport): Long
}
