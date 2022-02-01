package com.faldez.shachi.database

import androidx.room.*
import com.faldez.shachi.model.SelectedServer
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM server_view")
    fun getAll(): Flow<List<ServerView>?>

    @Query("SELECT * FROM server_view WHERE selected = :selected")
    fun getSelectedServer(selected: Boolean = true): Flow<ServerView?>


    @Query("SELECT * FROM server_view WHERE server_id = :serverId")
    fun getServer(serverId: Int): Flow<ServerView?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedServer(server: SelectedServer)

    @Insert
    suspend fun insert(server: Server)

    @Update
    suspend fun update(server: Server)

    @Delete
    suspend fun delete(server: Server)
}