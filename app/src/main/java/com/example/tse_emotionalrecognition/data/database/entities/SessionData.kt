package com.example.tse_emotionalrecognition.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SessionData(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var startTimeMillis: Long = 0L,
    var endTimeMillis: Long = 0L,
    var count: Int = 0,
    var synced:Long = 0L
)

enum class SessionDataColumns {
    START_TIME_MILLIS,
    END_TIME_MILLIS,
    SYNCED,
    COUNT
}