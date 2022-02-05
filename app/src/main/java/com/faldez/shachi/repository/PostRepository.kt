package com.faldez.shachi.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.response.mapToPost
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception

class PostRepository constructor(
    private val service: BooruService,
) {
    fun getSearchPostsResultStream(action: Action.SearchPost): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                PostPagingSource(action, service)
            }
        ).flow
    }

    suspend fun getSavedSearchPosts(action: Action.SearchSavedSearchPost): Pair<SavedSearchServer, List<Post>?> {
        try {
            when (action.savedSearch.server.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(0, 50).toString()

                    Log.d("PostPagingSource/Gelbooru", url)

                    val posts =
                        service.gelbooru.getPosts(url).mapToPost(action.savedSearch.server.serverId)

                    return Pair(action.savedSearch, posts)
                }
                ServerType.Danbooru -> {
                    val url = action.buildDanbooruUrl(0, 50).toString()

                    Log.d("PostPagingSource/Danbooru", url)

                    val posts =
                        service.danbooru2.getPosts(url).mapToPost(action.savedSearch.server.serverId)

                    return Pair(action.savedSearch, posts)
                }
            }
        } catch (exception: IOException) {
            throw Error(exception)
        } catch (exception: HttpException) {
            throw Error(exception)
        }
    }

    suspend fun testSearchPost(action: Action.SearchPost) {
        try {
            when (action.server?.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(0).toString()
                    Log.d("PostPagingSource/Gelbooru", url)
                    if (service.gelbooru.getPosts(url).mapToPost(action.server.serverId)
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
                ServerType.Danbooru -> {
                    val url = action.buildDanbooruUrl(1).toString()
                    Log.d("PostPagingSource/Danbooru", url)
                    if (service.danbooru2.getPosts(url).mapToPost(action.server.serverId)
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
                null -> {
                    throw Error("server not found")
                }
            }
        } catch (exception: Exception) {
            throw Error(exception)
        }
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 100
    }
}