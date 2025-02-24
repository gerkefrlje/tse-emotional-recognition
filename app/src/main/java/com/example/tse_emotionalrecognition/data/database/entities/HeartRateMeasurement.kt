package com.example.tse_emotionalrecognition.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class HeartRateMeasurement(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var sessionId: Long,
    var timestamp: Long,
    var hr: Int,
    var hrIbi: MutableList<Int>,
    var hrStatus: Int,
    var ibiStatus: MutableList<Int>,
    var synced: Long = 0L
)

