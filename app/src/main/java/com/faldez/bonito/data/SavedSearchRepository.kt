package com.faldez.bonito.data

import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.model.SavedSearch
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