package com.faldez.sachi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.faldez.sachi.model.SavedSearch
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_search")
    fun getAll(): Flow<List<SavedSearch>?>

    @Insert
    suspend fun insert(savedSearch: SavedSearch)

    @Delete
    suspend fun delete(savedSearch: SavedSearch)
}