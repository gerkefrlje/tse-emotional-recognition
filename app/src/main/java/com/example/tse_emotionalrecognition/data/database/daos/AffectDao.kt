package com.example.tse_emotionalrecognition.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tse_emotionalrecognition.data.database.entities.AffectData

@Dao
interface AffectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(affect: AffectData): Long
    @Query("SELECT * FROM affectData WHERE id = :id")
    fun getAffectById(id: Long): AffectData;
    @Query("SELECT * FROM affectData")
    fun getAll(): List<AffectData>
}