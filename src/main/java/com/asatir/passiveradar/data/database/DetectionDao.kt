package com.asatir.passiveradar.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.asatir.passiveradar.data.model.DetectionRecord

@Dao
interface DetectionDao {
    @Insert
    suspend fun insertDetection(detection: DetectionRecord): Long

    @Query("SELECT * FROM detection_records ORDER BY timestamp DESC")
    suspend fun getAllDetections(): List<DetectionRecord>

    @Query("SELECT * FROM detection_records WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getDetectionsByTimeRange(startTime: Long, endTime: Long): List<DetectionRecord>

    @Query("SELECT * FROM detection_records ORDER BY confidence DESC LIMIT :limit")
    suspend fun getTopDetections(limit: Int): List<DetectionRecord>

    @Query("DELETE FROM detection_records")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM detection_records")
    suspend fun getCount(): Int
}
