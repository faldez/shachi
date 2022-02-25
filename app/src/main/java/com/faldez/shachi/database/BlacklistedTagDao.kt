package com.faldez.shachi.database

import androidx.room.*
import com.faldez.shachi.model.BlacklistedTag
import com.faldez.shachi.model.BlacklistedTagWithServer
import com.faldez.shachi.model.ServerBlacklistedTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistedTagDao {
    @Query("SELECT * FROM blacklisted_tag")
    fun getAllFlow(): Flow<List<BlacklistedTagWithServer>?>

    @Query("SELECT * FROM blacklisted_tag")
    suspend fun getAll(): List<BlacklistedTagWithServer>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklistedTag(blacklistedTag: BlacklistedTag): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBlacklistTag(serverBlacklistedTagCrossRefs: List<ServerBlacklistedTagCrossRef>): Array<Long>

    @Delete
    suspend fun deleteBlacklistTag(blacklistedTag: BlacklistedTag)
}