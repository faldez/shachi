package com.faldez.shachi.data.repository

import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.SelectedServer
import com.faldez.shachi.data.model.Server
import com.faldez.shachi.data.model.ServerView
import kotlinx.coroutines.flow.Flow

class ServerRepository(private val db: AppDatabase) {
    fun getAllServersFlow(): Flow<List<ServerView>?> {
        return db.serverDao().getAllFlow()
    }

    suspend fun getAllServers(): List<Server>? {
        return db.serverDao().getAll()
    }

    fun getSelectedServer(): Flow<ServerView?> {
        return db.serverDao().getSelectedServer()
    }

    fun getServer(serverId: Int): Flow<ServerView?> {
        return db.serverDao().getServer(serverId)
    }

    suspend fun getServerById(serverId: Int): ServerView? {
        return db.serverDao().getServerById(serverId)
    }

    suspend fun setSelectedServer(serverId: Int) {
        return db.serverDao().insertSelectedServer(SelectedServer(serverId = serverId))
    }

    suspend fun insert(server: Server): Long {
        return db.serverDao().insert(server)
    }

    suspend fun update(server: Server) {
        return db.serverDao().update(server)
    }

    suspend fun delete(server: Server) {
        return db.serverDao().delete(server)
    }
}