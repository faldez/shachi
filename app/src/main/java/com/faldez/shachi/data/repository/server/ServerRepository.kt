package com.faldez.shachi.data.repository

import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.SelectedServer
import com.faldez.shachi.data.model.Server
import com.faldez.shachi.data.model.ServerView
import kotlinx.coroutines.flow.Flow

interface ServerRepository {
    val db: AppDatabase

    fun getAllServersFlow(): Flow<List<ServerView>?> = db.serverDao().getAllFlow()

    suspend fun getAllServers(): List<ServerView>? = db.serverDao().getAll()

    fun getSelectedServer(): Flow<ServerView?> = db.serverDao().getSelectedServer()

    fun getServer(serverId: Int): Flow<ServerView?> = db.serverDao().getServer(serverId)

    suspend fun getServerById(serverId: Int): ServerView? = db.serverDao().getServerById(serverId)

    suspend fun getServerByUrl(url: String): ServerView? = db.serverDao().getServerByUrl(url)

    suspend fun setSelectedServer(serverId: Int) =
        db.serverDao().insertSelectedServer(SelectedServer(serverId = serverId))

    suspend fun insert(server: Server): Long = db.serverDao().insert(server)

    suspend fun update(server: Server) = db.serverDao().update(server)

    suspend fun delete(server: Server) = db.serverDao().delete(server)
}

