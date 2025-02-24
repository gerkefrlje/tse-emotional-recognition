package com.example.teamprojekttest.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SkinTemperature(

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var sessionId: Long,
    var timestamp: Long,
    var synced: Long = 0L


)
