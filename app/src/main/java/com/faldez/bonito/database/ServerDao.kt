package com.faldez.bonito.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.faldez.bonito.model.Server
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM server")
    fun getAll(): Flow<List<Server>?>

    @Insert
    fun insert(server: Server)

    @Delete
    fun delete(server: Server)
}