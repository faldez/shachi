package com.faldez.bonito.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.faldez.bonito.model.Post
import com.faldez.bonito.model.SelectedServer
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerWithSelected

@Database(entities = [Server::class, SelectedServer::class, Post::class],
    views = [ServerWithSelected::class],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao

    companion object {
        private var db: AppDatabase? = null
        fun build(context: Context): AppDatabase {
            if (db == null) {
                db = Room.databaseBuilder(context, AppDatabase::class.java, "bonito")
                    .fallbackToDestructiveMigration().build()
            }
            return db!!
        }
    }
}