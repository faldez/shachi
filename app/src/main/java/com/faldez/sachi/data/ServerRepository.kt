package com.faldez.sachi.data

import com.faldez.sachi.database.AppDatabase
import com.faldez.sachi.model.SelectedServer
import com.faldez.sachi.model.Server
import com.faldez.sachi.model.ServerWithSelected
import kotlinx.coroutines.flow.Flow

class ServerRepository(private val db: AppDatabase) {
    fun getAllServers(): Flow<List<ServerWithSelected>?> {
        return db.serverDao().getAll()
    }

    fun getSelectedServer(): Flow<ServerWithSelected?> {
        return db.serverDao().getSelectedServer()
    }

    suspend fun setSelectedServer(serverId: Int) {
        return db.serverDao().insertSelectedServer(SelectedServer(serverId = serverId))
    }

    suspend fun insert(server: Server) {
        return db.serverDao().insert(server)
    }

    suspend fun update(server: Server) {
        return db.serverDao().update(server)
    }

    suspend fun delete(server: Server) {
        return db.serverDao().delete(server)
    }
}