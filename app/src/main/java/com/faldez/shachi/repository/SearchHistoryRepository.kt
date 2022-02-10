package com.faldez.shachi.repository

import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.SearchHistory
import com.faldez.shachi.model.SearchHistoryServer
import kotlinx.coroutines.flow.Flow

class SearchHistoryRepository(private val db: AppDatabase) {
    suspend fun insert(searchHistory: SearchHistory) = db.searchHistoryDao().insert(searchHistory)

    suspend fun delete(searchHistory: SearchHistory) = db.searchHistoryDao().delete(searchHistory)

    fun getAll(): Flow<List<SearchHistoryServer>?> = db.searchHistoryDao().getAll()
}