package com.gramavaxi.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class Animal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String,
    val breed: String,
    val ageMonths: Int,
    val gender: String,
    val photoPath: String?,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
)
