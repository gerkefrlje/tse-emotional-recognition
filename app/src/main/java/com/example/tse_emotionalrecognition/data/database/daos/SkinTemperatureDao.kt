package com.example.teamprojekttest.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import com.example.teamprojekttest.data.database.entities.SkinTemperature

@Dao
interface SkinTemperatureDao {

    @Insert
    suspend fun insert(item: SkinTemperature): Long

    @Insert
    suspend fun insertAll(items: List<SkinTemperature>): List<Long>

}