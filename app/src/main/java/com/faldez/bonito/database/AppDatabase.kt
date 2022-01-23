package com.faldez.bonito.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.faldez.bonito.model.Server

@Database(entities = [Server::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao

    companion object {
        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "bonito").build()
        }
    }
}