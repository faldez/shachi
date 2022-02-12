package com.faldez.shachi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_search")
    suspend fun getAll(): List<SavedSearchServer>?

    @Query("SELECT * FROM saved_search")
    fun getAllFlow(): Flow<List<SavedSearchServer>?>

    @Insert
    suspend fun insert(savedSearch: SavedSearch)

    @Delete
    suspend fun delete(savedSearch: SavedSearch)
}