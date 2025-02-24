package com.example.tse_emotionalrecognition.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tse_emotionalrecognition.data.database.daos.AffectDao
import com.example.tse_emotionalrecognition.data.database.daos.HeartRateMeasurementDao
import com.example.tse_emotionalrecognition.data.database.daos.SkinTemperatureMeasurementDao
import com.example.tse_emotionalrecognition.data.database.entities.HeartRateMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.SkinTemperatureMeasurement
import com.example.tse_emotionalrecognition.data.database.entities.AffectData


@Database(
    version = 1,
    entities = [
        AffectData::class,
        SkinTemperatureMeasurement::class,
        HeartRateMeasurement::class
    ]
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun getAffectDao(): AffectDao
    abstract fun getSkinTemperatureMeasurementDao(): SkinTemperatureMeasurementDao
    abstract fun getHeartRateMeasurementDao(): HeartRateMeasurementDao
    // SessionData may also need to be added
}

