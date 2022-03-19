package com.faldez.shachi.data.repository.post

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.ServerType
import com.faldez.shachi.data.model.response.mapToPost
import com.faldez.shachi.data.api.Action
import com.faldez.shachi.data.api.BooruApi
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    val booruApi: BooruApi

    fun getSearchPostsResultStream(action: Action.SearchPost): Flow<PagingData<Post>> = Pager(
        config = PagingConfig(
            pageSize = action.limit,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            PostPagingSource(action, booruApi)
        }
    ).flow

    suspend fun testSearchPost(action: Action.SearchPost) {
        try {
            when (action.server.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(0).toString()
                    Log.d("PostPagingSource/Gelbooru", url)
                    if (booruApi.gelbooru.getPosts(url).mapToPost(action.server.toServer())
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
                ServerType.Danbooru -> {
                    val url = action.buildDanbooruUrl(1).toString()
                    Log.d("PostPagingSource/Danbooru", url)
                    if (booruApi.danbooru.getPosts(url).mapToPost(action.server.toServer())
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
                ServerType.Moebooru -> {
                    val url = action.buildMoebooruUrl(1).toString()
                    Log.d("PostPagingSource/Moebooru", url)
                    if (booruApi.moebooru.getPosts(url).mapToPost(action.server.toServer())
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
            }
        } catch (exception: Exception) {
            throw Error(exception)
        }
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 50
    }
}