package com.example.tse_emotionalrecognition.common.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tse_emotionalrecognition.common.data.database.entities.InterventionStats
import com.example.tse_emotionalrecognition.common.data.database.entities.TAG

@Dao
interface InterventionStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InterventionStats): Long

    @Update
    suspend fun update(item: InterventionStats)

    @Query("SELECT * FROM InterventionStats WHERE tag = :tag")
    suspend fun getInterventionStatsByTag(tag: TAG = TAG.NONE): InterventionStats

    @Query("SELECT * FROM InterventionStats WHERE id = :id")
    suspend fun getInterventionStatsById(id: Long): InterventionStats

}