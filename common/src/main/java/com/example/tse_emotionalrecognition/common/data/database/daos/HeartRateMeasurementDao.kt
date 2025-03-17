package com.example.tse_emotionalrecognition.common.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement

@Dao
interface HeartRateMeasurementDao {

    @Insert
    suspend fun insert(item: HeartRateMeasurement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HeartRateMeasurement>): List<Long>

    @Query("SELECT * FROM heartRateMeasurement WHERE synced = :syncedValue")
    suspend fun getItemsBySyncedValue(syncedValue: Long=1L): List<HeartRateMeasurement>

    @Query("SELECT * FROM heartRateMeasurement")
    suspend fun getAll(): List<HeartRateMeasurement>


}