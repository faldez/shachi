package com.faldez.shachi.repository

import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.BlacklistedTag
import com.faldez.shachi.model.BlacklistedTagWithServer
import com.faldez.shachi.model.ServerBlacklistedTagCrossRef
import kotlinx.coroutines.flow.Flow

class BlacklistTagRepository(private val db: AppDatabase) {
    fun getAllFlow(): Flow<List<BlacklistedTagWithServer>?> {
        return db.blacklistedTagDao().getAllFlow()
    }

    suspend fun getAll(): List<BlacklistedTagWithServer>? {
        return db.blacklistedTagDao().getAll()
    }

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