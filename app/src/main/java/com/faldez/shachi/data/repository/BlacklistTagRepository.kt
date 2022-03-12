package com.faldez.shachi.data.repository

import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.BlacklistedTag
import com.faldez.shachi.data.model.BlacklistedTagWithServer
import com.faldez.shachi.data.model.ServerBlacklistedTagCrossRef
import kotlinx.coroutines.flow.Flow

class BlacklistTagRepository(private val db: AppDatabase) {
    fun getAllFlow(): Flow<List<BlacklistedTagWithServer>?> {
        return db.blacklistedTagDao().getAllFlow()
    }

    suspend fun getAll(): List<BlacklistedTagWithServer>? {
        return db.blacklistedTagDao().getAll()
    }

    suspend fun getByTags(tags: String): BlacklistedTag? = db.blacklistedTagDao().getByTags(tags)

    suspend fun insertBlacklistedTag(blacklistedTag: BlacklistedTag): Long {
        return db.blacklistedTagDao().insertBlacklistedTag(blacklistedTag)
    }

    suspend fun insertBlacklistedTag(serverBlacklistedTagCrossRefs: List<ServerBlacklistedTagCrossRef>) {
        db.blacklistedTagDao().insertBlacklistTag(serverBlacklistedTagCrossRefs)
    }


    suspend fun deleteBlacklistedTag(blacklistedTag: BlacklistedTag) {
        db.blacklistedTagDao().deleteBlacklistTag(blacklistedTag)
    }
}