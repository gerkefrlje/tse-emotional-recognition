package com.example.tse_emotionalrecognition.common.data.database

import androidx.room.*


@Database(
    version = 2,
    entities = [
        com.example.tse_emotionalrecognition.common.data.database.entities.AffectData::class,
        com.example.tse_emotionalrecognition.common.data.database.entities.SkinTemperatureMeasurement::class,
        com.example.tse_emotionalrecognition.common.data.database.entities.HeartRateMeasurement::class,
        com.example.tse_emotionalrecognition.common.data.database.entities.SessionData::class,
        com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats::class
    ]
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun getAffectDao(): com.example.tse_emotionalrecognition.common.data.database.daos.AffectDao
    abstract fun getSkinTemperatureMeasurementDao(): com.example.tse_emotionalrecognition.common.data.database.daos.SkinTemperatureMeasurementDao
    abstract fun getHeartRateMeasurementDao(): com.example.tse_emotionalrecognition.common.data.database.daos.HeartRateMeasurementDao
    abstract fun getSessionDao(): com.example.tse_emotionalrecognition.common.data.database.daos.SessionDao
    abstract fun getInterventionStatsDao(): com.example.tse_emotionalrecognition.common.data.database.daos.InterventionStatsDao
}

