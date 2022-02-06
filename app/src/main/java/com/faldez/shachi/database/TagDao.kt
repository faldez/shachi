package com.faldez.shachi.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.faldez.shachi.model.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTags(tags: List<Tag>)

    @Query("SELECT * FROM tag WHERE name = :name")
    suspend fun getTag(name: String): Tag?

    @RawQuery
    suspend fun getTags(query: SupportSQLiteQuery): List<Tag>?
}