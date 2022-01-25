package com.faldez.bonito.data

import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.database.ServerDao
import com.faldez.bonito.model.SelectedServer
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerWithSelected
import kotlinx.coroutines.flow.Flow

class ServerRepository(private val db: AppDatabase) {
    fun getAllServers(): Flow<List<ServerWithSelected>?> {
        return db.serverDao().getAll()
    }

    fun getSelectedServer(): Flow<ServerWithSelected?> {
        return db.serverDao().getSelectedServer()
    }

    suspend fun setSelectedServer(serverUrl: String) {
        return db.serverDao().insertSelectedServer(SelectedServer(serverUrl = serverUrl))
    }

    suspend fun insert(server: Server) {
        return db.serverDao().insert(server)
    }

    suspend fun delete(server: Server) {
        return db.serverDao().delete(server)
    }
}