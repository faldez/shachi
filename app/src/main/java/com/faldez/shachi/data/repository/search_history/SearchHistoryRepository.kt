package com.faldez.shachi.data.repository.search_history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.SearchHistory
import com.faldez.shachi.data.model.SearchHistoryServer
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    val db: AppDatabase
    suspend fun insert(searchHistory: SearchHistory) = db.searchHistoryDao().insert(searchHistory)

    suspend fun delete(searchHistory: SearchHistory) = db.searchHistoryDao().delete(searchHistory)

    fun getAllFlow(): Flow<PagingData<SearchHistoryServer>> = Pager(
        config = PagingConfig(
            pageSize = 5,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            db.searchHistoryDao().getAllPagingData()
        }
    ).flow

    suspend fun getAll(): List<SearchHistory>? = db.searchHistoryDao().getAll()
}