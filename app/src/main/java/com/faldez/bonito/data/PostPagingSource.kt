package com.faldez.bonito.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.faldez.bonito.data.Repository.Companion.STARTING_PAGE_INDEX
import com.faldez.bonito.model.Post
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerType
import com.faldez.bonito.service.Action
import com.faldez.bonito.service.BooruService
import com.faldez.bonito.service.GelbooruService
import retrofit2.HttpException
import java.io.IOException

class PostPagingSource(
    private val action: Action.SearchPost,
    private val service: BooruService,
) :
    PagingSource<Int, Post>() {
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = when (action.server.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(position).toString()
                    Log.d("PostPagingSource", url)
                    service.gelbooru.getPosts(url)
                }
                ServerType.Danbooru -> {
                    TODO("not yet implemented")
                }
            }
            val posts = response.posts.post ?: listOf()
            val nextKey = if (posts?.isEmpty()) {
                null
            } else {
                position + 1
            }

            return LoadResult.Page(data = posts,
                prevKey = if (position == STARTING_PAGE_INDEX) null else position,
                nextKey = nextKey)
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}