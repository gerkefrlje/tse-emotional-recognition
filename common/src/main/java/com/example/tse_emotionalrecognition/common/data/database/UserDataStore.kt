package com.example.tse_emotionalrecognition.common.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object UserDataStore {

    private var roomDb: com.example.tse_emotionalrecognition.common.data.database.UserDatabase? = null

    private fun getDB(context: Context) : com.example.tse_emotionalrecognition.common.data.database.UserDatabase {
        if (roomDb == null) {
            roomDb = Room.databaseBuilder(context,
                com.example.tse_emotionalrecognition.common.data.database.UserDatabase::class.java, "data.db")
                .fallbackToDestructiveMigration()
                .addCallback(object: RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        roomDb?.let{
                            val repo = UserRepository(it)
                        }
                    }
                })
                .build()
        }
        return roomDb!!
    }

    fun getUserRepository(context: Context) = UserRepository(getDB(context))

}
