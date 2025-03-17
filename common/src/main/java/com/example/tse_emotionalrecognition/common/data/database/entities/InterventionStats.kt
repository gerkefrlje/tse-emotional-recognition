package com.example.tse_emotionalrecognition.common.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity
@Serializable
data class InterventionStats(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var tag: TAG = TAG.NONE,
    var triggeredCount: Int = 0,
    var dismissedCount: Int = 0
)

enum class TAG{
    INTERVENTIONS,
    NONE
}

