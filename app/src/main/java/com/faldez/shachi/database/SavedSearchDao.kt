package com.faldez.shachi.database

import androidx.room.*
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_search")
    fun getAll(): Flow<List<SavedSearchServer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savedSearch: SavedSearch)

    @Delete
    suspend fun delete(savedSearch: SavedSearch)
}