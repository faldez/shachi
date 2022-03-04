package com.faldez.shachi.repository

import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import kotlinx.coroutines.flow.Flow

class SavedSearchRepository(private val db: AppDatabase) {
    fun getAllFlow(): Flow<List<SavedSearchServer>> {
        return db.savedSearchDao().getAllFlow()
    }

    suspend fun getAll(): List<SavedSearch> {
        return db.savedSearchDao().getAll()
    }

    suspend fun insert(tags: String, title: String, serverId: Int) {
        db.savedSearchDao().insert(tags, title, serverId)
    }

    suspend fun delete(savedSearch: SavedSearch) {
        db.savedSearchDao().delete(savedSearch)
    }
}