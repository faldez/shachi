package com.faldez.bonito.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.faldez.bonito.model.Post
import com.faldez.bonito.service.GelbooruService
import okio.IOException
import retrofit2.HttpException

class GelbooruPagingSource(private val service: GelbooruService, private val tags: String) :
    PagingSource<Int, Post>() {
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val position = params.key ?: 1
        return try {
            val response = service.getPosts(position, tags)
            val posts = response.posts.post
            val nextKey = if (posts?.isEmpty() == true) {
                null
            } else {
                position + 1
            }

            return LoadResult.Page(data = posts!!,
                prevKey = if (position == 1) null else position,
                nextKey = nextKey)
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}