package com.faldez.shachi.data.repository.favorite

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface FavoriteRepository {
    val db: AppDatabase

    suspend fun insert(favorite: Post) = db.favoriteDao().insert(favorite)

    suspend fun delete(favorite: Post) = db.favoriteDao().delete(favorite)

    fun query(tags: String): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(
            pageSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            val query = tags.split(" ").mapNotNull { if (it.isNullOrEmpty()) null else it }
                .joinToString(separator = " ") { "tags:$it" }
            if (query.isEmpty()) {
                db.favoriteDao().query()
            } else {
                db.favoriteDao().queryByTags(query)
            }
        }
    ).flow

    fun queryByServerUrlAndPostIds(serverId: Int, postIds: List<Int>): Flow<Set<Int>> =
        db.favoriteDao().queryByServerUrlAndPostIds(serverId, postIds).map { it.toSet() }

    suspend fun queryByServerUrlAndPostId(serverId: Int, postId: Int): Int? =
        db.favoriteDao().queryByServerUrlAndPostId(serverId, postId)

    suspend fun getAll(): List<Post> = db.favoriteDao().getAll()

}