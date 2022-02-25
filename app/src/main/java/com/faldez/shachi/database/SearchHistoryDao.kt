package com.faldez.shachi.database

import androidx.paging.PagingSource
import androidx.room.*
import com.faldez.shachi.model.SearchHistory
import com.faldez.shachi.model.SearchHistoryServer

@Dao
interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(searchHistory: SearchHistory)

    @Delete
    suspend fun delete(searchHistory: SearchHistory)

    @Query("SELECT * FROM search_history ORDER BY created_at DESC")
    fun getAllPagingData(): PagingSource<Int, SearchHistoryServer>

    @Query("SELECT * FROM search_history ORDER BY created_at DESC")
    suspend fun getAll(): List<SearchHistory>?
}