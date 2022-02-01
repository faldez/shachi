package com.faldez.shachi.data

import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.SelectedServer
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView
import kotlinx.coroutines.flow.Flow

class ServerRepository(private val db: AppDatabase) {
    fun getAllServers(): Flow<List<ServerView>?> {
        return db.serverDao().getAll()
    }

    fun getSelectedServer(): Flow<ServerView?> {
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