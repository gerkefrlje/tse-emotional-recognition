package com.example.tse_emotionalrecognition.data.database

import androidx.room.*
import com.example.tse_emotionalrecognition.data.database.daos.AffectDao
import com.example.tse_emotionalrecognition.data.database.daos.SessionDao
import com.example.tse_emotionalrecognition.data.database.daos.HeartRateMeasurementDao
import com.example.tse_emotionalrecognition.data.database.daos.SkinTemperatureMeasurementDao
import com.example.tse_emotionalrecognition.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.SkinTemperatureMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.AffectData
import com.example.tse_emotionalrecognition.data.database.entities.SessionData


@Database(
    version = 1,
    entities = [
        AffectData::class,
        SkinTemperatureMeasurement::class,
        HeartRateMeasurement::class,
        SessionData::class
    ]
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun getAffectDao(): AffectDao
    abstract fun getSkinTemperatureMeasurementDao(): SkinTemperatureMeasurementDao
    abstract fun getHeartRateMeasurementDao(): HeartRateMeasurementDao
    abstract fun getSessionDao(): SessionDao
}

