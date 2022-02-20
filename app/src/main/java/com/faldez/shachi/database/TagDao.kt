package com.faldez.shachi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faldez.shachi.model.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTag(tag: Tag)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertTags(tags: List<Tag>)

    @Query("SELECT * FROM tag WHERE name = :name")
    suspend fun getTag(name: String): Tag?

    @Query("SELECT * FROM tag WHERE name LIKE :name LIMIT :limit")
    suspend fun searchTag(name: String, limit: Int = 10): List<Tag>?

    @Query("SELECT * FROM tag WHERE name IN (:names)")
    suspend fun getTags(names: List<String>): List<Tag>?
}