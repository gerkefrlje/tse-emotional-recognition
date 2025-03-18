package com.example.tse_emotionalrecognition.common.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class SkinTemperatureMeasurement(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var sessionId: Long,
    var timestamp: Long,
    var objectTemperature: Float,
    var ambientTemperature: Float,
    var status: Int,
    var synced: Long = 0L


)
