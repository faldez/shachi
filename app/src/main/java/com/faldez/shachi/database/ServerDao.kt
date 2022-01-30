package com.faldez.shachi.database

import androidx.room.*
import com.faldez.shachi.model.SelectedServer
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerWithSelected
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM server_with_selected")
    fun getAll(): Flow<List<ServerWithSelected>?>

    @Query("SELECT * FROM server_with_selected WHERE selected = :selected")
    fun getSelectedServer(selected: Boolean = true): Flow<ServerWithSelected?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedServer(server: SelectedServer)

    @Insert
    suspend fun insert(server: Server)

    @Update
    suspend fun update(server: Server)

    @Delete
    suspend fun delete(server: Server)
}