package com.faldez.bonito.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.faldez.bonito.model.*

@Database(entities = [Server::class, SelectedServer::class, Post::class],
    views = [ServerWithSelected::class],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        private var db: AppDatabase? = null
        fun build(context: Context): AppDatabase =
            db ?: synchronized(this) {
                val newDb = db ?: Room.databaseBuilder(context, AppDatabase::class.java, "bonito")
                    .fallbackToDestructiveMigration().build().also { db = it }
                newDb
            }
    }
}