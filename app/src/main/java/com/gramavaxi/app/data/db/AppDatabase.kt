package com.gramavaxi.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gramavaxi.app.data.dao.AnimalDao
import com.gramavaxi.app.data.dao.CampDao
import com.gramavaxi.app.data.dao.DiseaseReportDao
import com.gramavaxi.app.data.dao.VaccinationDao
import com.gramavaxi.app.data.entity.Animal
import com.gramavaxi.app.data.entity.Camp
import com.gramavaxi.app.data.entity.DiseaseReport
import com.gramavaxi.app.data.entity.Vaccination

@Database(
    entities = [Animal::class, Vaccination::class, Camp::class, DiseaseReport::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animalDao(): AnimalDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun campDao(): CampDao
    abstract fun diseaseReportDao(): DiseaseReportDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "gramavaxi.db"
            ).build().also { INSTANCE = it }
        }
    }
}
