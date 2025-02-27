package com.example.phone.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity
@Serializable
data class HeartRateMeasurement(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var sessionId: Long,
    var timestamp: Long,
    var hr: Int,
    var hrStatus: Int,
    var synced: Long = 0L
)

