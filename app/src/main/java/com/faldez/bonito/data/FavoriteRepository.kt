package com.faldez.bonito.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(private val db: AppDatabase) {
    suspend fun insert(favorite: Post) {
        return db.favoriteDao().insert(favorite)
    }

    suspend fun delete(favorite: Post) {
        return db.favoriteDao().delete(favorite)
    }

    fun query(tags: String): Flow<PagingData<Post>> {
        val query = tags.map { "tags:$it" }.joinToString(separator = " ")
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                if (query.isEmpty()) {
                    db.favoriteDao().query()
                } else {
                    db.favoriteDao().queryByTags(query)
                }
            }
        ).flow
    }

    fun queryByServerUrlAndPostIds(serverUrl: String, postIds: List<Int>): Flow<Set<Int>> {
        return db.favoriteDao().queryByServerUrlAndPostIds(serverUrl, postIds).map { it.toSet() }
    }

    suspend fun queryByServerUrlAndPostId(serverUrl: String, postId: Int): Int? {
        return db.favoriteDao().queryByServerUrlAndPostId(serverUrl, postId)
    }
}