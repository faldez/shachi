package com.faldez.bonito.data

import android.util.Log
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.model.Favorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository(private val db: AppDatabase) {
    suspend fun insert(favorite: Favorite) {
        Log.d("FavoriteRepository", "insert $favorite")
        return db.favoriteDao().insert(favorite)
    }

    suspend fun delete(favorite: Favorite) {
        Log.d("FavoriteRepository", "delete $favorite")
        return db.favoriteDao().delete(favorite)
    }

    fun queryByServerUrlAndPostIds(serverUrl: String, postIds: List<Int>): Flow<Set<Int>> {
        Log.d("FavoriteRepository", "queryByServerUrlAndPostIds $serverUrl $postIds")
        return db.favoriteDao().queryByServerUrlAndPostIds(serverUrl, postIds).map { it.toSet() }
    }

    suspend fun queryByServerUrlAndPostId(serverUrl: String, postId: Int): Int? {
        Log.d("FavoriteRepository", "queryByServerUrlAndPostId $serverUrl $postId")
        return db.favoriteDao().queryByServerUrlAndPostId(serverUrl, postId)
    }
}