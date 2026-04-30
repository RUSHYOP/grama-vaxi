package com.gramavaxi.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "camps")
data class Camp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val location: String,
    val date: Long,
    val vaccinesOffered: String,
    val notes: String? = null
)
