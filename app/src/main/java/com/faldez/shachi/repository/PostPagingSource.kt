package com.faldez.shachi.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.response.*
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService
import com.faldez.shachi.service.Danbooru2Service
import com.faldez.shachi.service.GelbooruService
import retrofit2.HttpException
import java.io.IOException

class PostPagingSource(
    private val action: Action.SearchPost,
    private val service: BooruService,
) :
    PagingSource<Int, Post>() {
    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1)
                ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val startingPageIndex = when (action.server?.type) {
            ServerType.Gelbooru -> GelbooruService.STARTING_PAGE_INDEX
            ServerType.Danbooru -> Danbooru2Service.STARTING_PAGE_INDEX
            else -> 1
        }
        val position = params.key ?: startingPageIndex
        try {
            val posts = when (action.server?.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(position).toString()
                    Log.d("PostPagingSource/Gelbooru", url)
                    service.gelbooru.getPosts(url).applyBlacklist(action.server.blacklistedTags)
                        ?.mapToPost(action.server.serverId) ?: listOf()
                }
                ServerType.Danbooru -> {
                    val url = action.buildDanbooruUrl(position).toString()
                    Log.d("PostPagingSource/Danbooru", url)
                    service.danbooru2.getPosts(url).applyBlacklist(action.server.blacklistedTags)
                        ?.mapToPost(action.server.serverId) ?: listOf()
                }
                null -> {
                    return LoadResult.Error(Error("server not found"))
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

    private fun GelbooruPostResponse.applyBlacklist(tags: String?): List<GelbooruPost>? {
        val blacklists = tags?.split(",")?.map {
            it.split(" ")
        }

        Log.d("PostPagingSource", "applyBlacklist $tags to $blacklists")

        if (blacklists.isNullOrEmpty()) {
            return this.posts?.post
        }

        var counter = 0
        val posts =  this.posts?.post?.filter { post ->
            blacklists.forEach { tags ->
                if (post.tags.split(" ").containsAll(tags)) {
                    counter++
                    return@filter false
                }
            }

            true
        }

        Log.d("PostPagingSource", "applyBlacklist $counter filtered")

        return posts
    }

    private fun List<Danbooru2Post>.applyBlacklist(tags: String?): List<Danbooru2Post>? {
        val blacklists = tags?.split(",")?.map {
            it.split(" ")
        }

        Log.d("PostPagingSource", "applyBlacklist $tags to $blacklists")

        if (blacklists.isNullOrEmpty()) {
            return this
        }

        var counter = 0
        val posts = this.filter { post ->
            blacklists.forEach { tags ->
                if (post.tagString.split(" ").containsAll(tags)) {
                    counter++
                    return@filter false
                }
            }

            true
        }

        Log.d("PostPagingSource", "applyBlacklist $counter filtered")

        return posts
    }
}