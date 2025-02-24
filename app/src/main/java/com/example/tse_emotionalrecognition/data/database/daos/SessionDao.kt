package com.example.tse_emotionalrecognition.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tse_emotionalrecognition.data.database.entities.SessionData

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionData): Long

    @Query("SELECT * FROM sessionData WHERE endTimeMillis=0 ORDER BY startTimeMillis DESC LIMIT 1")
    fun getActiveSession(): SessionData

    @Query("SELECT * FROM sessionData WHERE id = :id")
    fun getSessionById(id: Long): SessionData
}