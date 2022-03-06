package com.faldez.shachi.data.database

import androidx.room.*
import com.faldez.shachi.data.model.SelectedServer
import com.faldez.shachi.data.model.Server
import com.faldez.shachi.data.model.ServerView
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM server_view")
    fun getAllFlow(): Flow<List<ServerView>?>

    @Query("SELECT * FROM server")
    suspend fun getAll(): List<Server>?

    @Query("SELECT * FROM server_view WHERE selected = :selected")
    fun getSelectedServer(selected: Boolean = true): Flow<ServerView?>


    @Query("SELECT * FROM server_view WHERE server_id = :serverId")
    fun getServer(serverId: Int): Flow<ServerView?>

    @Query("SELECT * FROM server_view WHERE server_id = :serverId")
    suspend fun getServerById(serverId: Int): ServerView?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedServer(server: SelectedServer)

    @Insert
    suspend fun insert(server: Server): Long

    @Update
    suspend fun update(server: Server)

    @Delete
    suspend fun delete(server: Server)
}