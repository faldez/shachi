package com.faldez.shachi.database

import androidx.room.*
import com.faldez.shachi.model.BlacklistedTag
import com.faldez.shachi.model.BlacklistedTagWithServer
import com.faldez.shachi.model.ServerBlacklistedTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface BlacklistedTagDao {
    @Query("SELECT * FROM blacklisted_tag")
    fun getAll(): Flow<List<BlacklistedTagWithServer>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlacklistedTag(blacklistedTag: BlacklistedTag): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBlacklistTag(serverBlacklistedTagCrossRefs: List<ServerBlacklistedTagCrossRef>): Array<Long>

    @Delete
    suspend fun deleteBlacklistTag(blacklistedTag: BlacklistedTag)
}