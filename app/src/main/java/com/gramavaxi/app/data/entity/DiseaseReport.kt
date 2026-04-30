package com.gramavaxi.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disease_reports")
data class DiseaseReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val animalId: Long,
    val animalName: String,
    val symptoms: String,
    val notes: String?,
    val photoPath: String?,
    val referenceId: String,
    val createdAt: Long = System.currentTimeMillis()
)
