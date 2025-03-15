package com.example.tse_emotionalrecognition.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tse_emotionalrecognition.data.database.entities.HeartRateMeasurement

@Dao
interface HeartRateMeasurementDao {

    @Insert
    suspend fun insert(item: HeartRateMeasurement): Long

    @Insert
    suspend fun insertAll(items: List<HeartRateMeasurement>): List<Long>

    @Query("SELECT * FROM heartRateMeasurement")
    fun getAll(): List<HeartRateMeasurement>

}