package com.faldez.shachi.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.ServerType
import com.faldez.shachi.data.model.response.mapToPost
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruServiceImpl
import kotlinx.coroutines.flow.Flow

class PostRepository constructor(
    private val service: BooruServiceImpl,
) {
    fun getSearchPostsResultStream(action: Action.SearchPost): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = action.limit,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PostPagingSource(action, service)
            }
        ).flow
    }

    suspend fun testSearchPost(action: Action.SearchPost) {
        try {
            when (action.server.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(0).toString()
                    Log.d("PostPagingSource/Gelbooru", url)
                    if (service.gelbooru.getPosts(url).mapToPost(action.server.toServer())
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
                ServerType.Danbooru -> {
                    val url = action.buildDanbooruUrl(1).toString()
                    Log.d("PostPagingSource/Danbooru", url)
                    if (service.danbooru.getPosts(url).mapToPost(action.server.toServer())
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
                ServerType.Moebooru -> {
                    val url = action.buildMoebooruUrl(1).toString()
                    Log.d("PostPagingSource/Moebooru", url)
                    if (service.moebooru.getPosts(url).mapToPost(action.server.toServer())
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