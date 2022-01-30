package com.faldez.sachi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.faldez.sachi.model.*

@Database(entities = [Server::class, SelectedServer::class, Post::class, SavedSearch::class],
    views = [ServerWithSelected::class],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun savedSearchDao(): SavedSearchDao

    companion object {
        private var db: AppDatabase? = null
        fun build(context: Context): AppDatabase =
            db ?: synchronized(this) {
                val newDb = db ?: Room.databaseBuilder(context, AppDatabase::class.java, "sachi")
                    .fallbackToDestructiveMigration().build().also { db = it }
                newDb
            }
    }
}