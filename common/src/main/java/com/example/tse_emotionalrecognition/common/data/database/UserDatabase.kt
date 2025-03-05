package com.example.tse_emotionalrecognition.common.data.database

import androidx.room.*


@Database(
    version = 1,
    entities = [
        com.example.tse_emotionalrecognition.common.data.database.entities.AffectData::class,
        com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement::class,
        com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement::class,
        com.example.tse_emotionalrecognition.common.data.database.entities.SessionData::class
    ]
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun getAffectDao(): com.example.tse_emotionalrecognition.common.data.database.daos.AffectDao
    abstract fun getSkinTemperatureMeasurementDao(): com.example.tse_emotionalrecognition.common.data.database.daos.SkinTemperatureMeasurementDao
    abstract fun getHeartRateMeasurementDao(): com.example.tse_emotionalrecognition.common.data.database.daos.HeartRateMeasurementDao
    abstract fun getSessionDao(): com.example.tse_emotionalrecognition.common.data.database.daos.SessionDao
}

