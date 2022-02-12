package com.faldez.shachi.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import kotlinx.coroutines.flow.Flow

class SavedSearchRepository(private val db: AppDatabase) {
    fun getAll(): Flow<List<SavedSearchServer>> {
        return db.savedSearchDao().getAll()
    }

    suspend fun insert(savedSearch: SavedSearch) {
        db.savedSearchDao().insert(savedSearch)
    }

    suspend fun delete(savedSearch: SavedSearch) {
        db.savedSearchDao().delete(savedSearch)
    }
}