package com.faldez.shachi.data.database

import androidx.room.*
import com.faldez.shachi.data.model.SavedSearch
import com.faldez.shachi.data.model.SavedSearchServer
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSearchDao {

    @Transaction
    @Query("SELECT * FROM saved_search")
    fun getAllFlow(): Flow<List<SavedSearchServer>>

    @Query("SELECT * FROM saved_search")
    suspend fun getAll(): List<SavedSearch>

    @Query("INSERT OR IGNORE INTO saved_search(" +
            "tags, " +
            "saved_search_title, " +
            "server_id, " +
            "saved_search_order) " +
            " VALUES(:tags, :title, :serverId," +
            "(SELECT IFNULL(MAX(saved_search_order), 0) + 1 FROM saved_search LIMIT 1)" +
            ")")
    suspend fun insert(tags: String, title: String, serverId: Int)

    @Update
    suspend fun update(savedSearch: SavedSearch)

    @Delete
    suspend fun delete(savedSearch: SavedSearch)
}