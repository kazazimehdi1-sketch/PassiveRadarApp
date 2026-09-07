package com.asatir.passiveradar.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.asatir.passiveradar.data.model.DetectionRecord

@Database(entities = [DetectionRecord::class], version = 1)
abstract class RadarDatabase : RoomDatabase() {
    abstract fun detectionDao(): DetectionDao

    companion object {
        @Volatile
        private var INSTANCE: RadarDatabase? = null

        fun getDatabase(context: Context): RadarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RadarDatabase::class.java,
                    "radar_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
