package com.faldez.sachi.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.faldez.sachi.model.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert
    suspend fun insert(favorite: Post)

    @Delete
    suspend fun delete(favorite: Post)

    @Query("SELECT * FROM favorite WHERE favorite MATCH :query")
    fun queryByTags(query: String): PagingSource<Int, Post>

    @Query("SELECT * FROM favorite")
    fun query(): PagingSource<Int, Post>

    @Query("SELECT post_id FROM favorite WHERE server_url = :serverUrl AND post_id IN (:postIds)")
    fun queryByServerUrlAndPostIds(serverUrl: String, postIds: List<Int>): Flow<List<Int>>


    @Query("SELECT post_id FROM favorite WHERE server_url = :serverUrl AND post_id = :postId")
    suspend fun queryByServerUrlAndPostId(serverUrl: String, postId: Int): Int?
}