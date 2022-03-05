package com.faldez.shachi.data.repository

import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.SavedSearch
import com.faldez.shachi.data.model.SavedSearchServer
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