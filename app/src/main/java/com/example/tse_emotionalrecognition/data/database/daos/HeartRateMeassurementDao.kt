package com.example.teamprojekttest.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import com.example.teamprojekttest.data.database.entities.HeartRateMeasurement

@Dao
interface HeartRateMeassurementDao {

    @Insert
    suspend fun insert(item: HeartRateMeasurement): Long

    @Insert
    suspend fun insertAll(items: List<HeartRateMeasurement>): List<Long>
}