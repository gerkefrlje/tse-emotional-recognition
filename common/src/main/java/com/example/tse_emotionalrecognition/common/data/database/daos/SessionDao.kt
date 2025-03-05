package com.example.tse_emotionalrecognition.common.data.database.daos

import androidx.room.*
import com.example.tse_emotionalrecognition.common.data.database.entities.SessionData

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionData): Long

    @Query("SELECT * FROM sessionData WHERE endTimeMillis=0 ORDER BY startTimeMillis DESC LIMIT 1")
    fun getActiveSession(): SessionData

    @Query("SELECT * FROM sessionData WHERE id = :id")
    fun getSessionById(id: Long): SessionData
}