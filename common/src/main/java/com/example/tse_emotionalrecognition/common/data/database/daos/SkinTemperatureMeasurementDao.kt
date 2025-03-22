package com.example.tse_emotionalrecognition.common.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement

@Dao
interface SkinTemperatureMeasurementDao {

    @Insert
    suspend fun insert(item: SkinTemperatureMeasurement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SkinTemperatureMeasurement>): List<Long>

    @Query("SELECT * FROM skinTemperatureMeasurement")
    suspend fun getAll(): List<SkinTemperatureMeasurement>


}