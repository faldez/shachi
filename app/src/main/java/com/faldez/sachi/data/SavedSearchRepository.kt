package com.faldez.sachi.data

import com.faldez.sachi.database.AppDatabase
import com.faldez.sachi.model.SavedSearch
import kotlinx.coroutines.flow.Flow

class SavedSearchRepository(private val db: AppDatabase) {
    fun getAll(): Flow<List<SavedSearch>?> {
        return db.savedSearchDao().getAll()
    }

    suspend fun insert(savedSearch: SavedSearch) {
        db.savedSearchDao().insert(savedSearch)
    }

    suspend fun delete(savedSearch: SavedSearch) {
        db.savedSearchDao().delete(savedSearch)
    }
}