package com.faldez.shachi.data.repository.post

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.faldez.shachi.data.api.*
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.ServerType
import com.faldez.shachi.data.model.response.mapToPost
import retrofit2.HttpException
import java.io.IOException

class PostPagingSource(
    private val action: Action.SearchPost,
    private val booruApi: BooruApi,
) :
    PagingSource<Int, Post>() {
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val startingPageIndex = when (action.server.type) {
            ServerType.Gelbooru -> GelbooruApi.STARTING_PAGE_INDEX
            ServerType.Danbooru -> DanbooruApi.STARTING_PAGE_INDEX
            ServerType.Moebooru -> MoebooruApi.STARTING_PAGE_INDEX
        }
        val position = params.key ?: startingPageIndex
        try {
            val posts = when (action.server.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(position).toString()
                    Log.d("PostPagingSource/Gelbooru", url)
                    booruApi.gelbooru.getPosts(url).mapToPost(action.server.toServer()) ?: listOf()
                }
                ServerType.Danbooru -> {
                    val url = action.buildDanbooruUrl(position).toString()
                    Log.d("PostPagingSource/Danbooru", url)
                    booruApi.danbooru.getPosts(url).mapToPost(action.server.toServer())
                }
                ServerType.Moebooru -> {
                    val url = action.buildMoebooruUrl(position).toString()
                    Log.d("PostPagingSource/Moebooru", url)
                    booruApi.moebooru.getPosts(url).mapToPost(action.server.toServer())
                }
            }

            val nextKey = if (posts.isEmpty()) {
                null
            } else {
                position + 1
            }

            return LoadResult.Page(data = posts,
                prevKey = if (position == startingPageIndex) null else position,
                nextKey = nextKey)
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}