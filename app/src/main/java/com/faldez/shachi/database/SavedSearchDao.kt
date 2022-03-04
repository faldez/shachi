package com.faldez.shachi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_search")
    fun getAllFlow(): Flow<List<SavedSearchServer>>

    @Query("SELECT * FROM saved_search")
    suspend fun getAll(): List<SavedSearch>

    @Query("INSERT INTO saved_search(" +
            "tags, " +
            "saved_search_title, " +
            "server_id, " +
            "saved_search_order) " +
            " VALUES(:tags, :title, :serverId," +
            "(SELECT IFNULL(MAX(saved_search_order), 0) + 1 FROM saved_search LIMIT 1)" +
            ")")
    suspend fun insert(tags: String, title: String, serverId: Int)

    @Delete
    suspend fun delete(savedSearch: SavedSearch)
}