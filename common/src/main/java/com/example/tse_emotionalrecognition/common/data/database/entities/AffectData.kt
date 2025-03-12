package com.example.tse_emotionalrecognition.common.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AffectData(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var sessionId: Long,
    var timeOfNotification: Long = 0L,
    var timeOfEngagement: Long = 0L,
    var timeOfAffect: Long = 0L,
    var timeOfFinished: Long = 0L,
    var affect: AffectType = AffectType.NONE
)

enum class AffectType {
    ANGRY_SAD,
    HAPPY_RELAXED,
    NONE
}


enum class AffectColumns {
    TIME_OF_ENGAGEMENT,
    AFFECT,
    TIME_OF_FINISHED
}
