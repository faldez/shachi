package com.faldez.shachi.database

import androidx.paging.PagingSource
import androidx.room.*
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.PostTag
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPost(favorite: Post)

    @Insert
    suspend fun insertPostTag(postTag: PostTag)

    @Transaction
    suspend fun insert(favorite: Post) {
        insertPost(favorite)
        insertPostTag(PostTag(
            serverId = favorite.serverId,
            postId = favorite.postId,
            tags = favorite.tags
        ))
    }

    @Delete
    suspend fun deletePost(favorite: Post)

    @Query("DELETE FROM post_tag WHERE server_id = :serverId AND post_id = :postId")
    suspend fun deletePostTag(serverId: Int, postId: Int)

    @Transaction
    suspend fun delete(favorite: Post) {
        deletePost(favorite)
        deletePostTag(
            favorite.serverId,
            favorite.postId,
        )
    }

    @Query("SELECT * FROM favorite NATURAL JOIN (SELECT server_id, post_id FROM post_tag WHERE post_tag MATCH :query) JOIN server ON favorite.server_id = server.server_id")
    fun queryByTags(query: String): PagingSource<Int, Post>

    @Query("SELECT * FROM favorite ORDER BY date_added DESC")
    fun query(): PagingSource<Int, Post>

    @Query("SELECT post_id FROM favorite WHERE server_id = :serverId AND post_id IN (:postIds)")
    fun queryByServerUrlAndPostIds(serverId: Int, postIds: List<Int>): Flow<List<Int>>


    @Query("SELECT post_id FROM favorite WHERE server_id = :serverId AND post_id = :postId")
    suspend fun queryByServerUrlAndPostId(serverId: Int, postId: Int): Int?
}