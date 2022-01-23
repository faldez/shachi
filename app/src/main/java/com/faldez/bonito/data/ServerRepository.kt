package com.faldez.bonito.data

import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.database.ServerDao
import com.faldez.bonito.model.Server
import kotlinx.coroutines.flow.Flow

class ServerRepository(private val db: AppDatabase) {
    fun getAllServers(): Flow<List<Server>?> {
        return db.serverDao().getAll()
    }

    fun insert(server: Server) {
        return db.serverDao().insert(server)
    }

    fun delete(server: Server) {
        return db.serverDao().delete(server)
    }
}