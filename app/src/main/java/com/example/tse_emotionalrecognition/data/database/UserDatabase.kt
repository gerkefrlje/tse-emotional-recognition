package com.example.teamprojekttest.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.teamprojekttest.data.database.daos.AffectDao
import com.example.teamprojekttest.data.database.daos.HeartRateMeassurementDao
import com.example.teamprojekttest.data.database.daos.SkinTemperatureDao
import com.example.teamprojekttest.data.database.entities.HeartRateMeasurement
import com.example.teamprojekttest.data.database.entities.SkinTemperature
import com.example.tse_emotionalrecognition.data.database.entities.AffectData


@Database(
    version = 1,
    entities = [
        AffectData::class,
        SkinTemperature::class,
        HeartRateMeasurement::class
    ]
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun getAffectDao(): AffectDao
    abstract fun getSkinTemperatureDao(): SkinTemperatureDao
    abstract fun getHeartRateMeassurementDao(): HeartRateMeassurementDao
}

