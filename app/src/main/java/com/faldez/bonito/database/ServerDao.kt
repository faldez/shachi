package com.faldez.bonito.database

import androidx.room.*
import com.faldez.bonito.model.SelectedServer
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerWithSelected
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM serverwithselected")
    fun getAll(): Flow<List<ServerWithSelected>?>

    @Query("SELECT * FROM serverwithselected WHERE selected = :selected")
    fun getSelectedServer(selected: Boolean = true): Flow<ServerWithSelected?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedServer(server: SelectedServer)

    @Insert
    suspend fun insert(server: Server)

    @Delete
    suspend fun delete(server: Server)
}