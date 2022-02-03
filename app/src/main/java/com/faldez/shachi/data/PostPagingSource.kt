package com.faldez.shachi.data

import android.util.Log
import androidx.core.os.LocaleListCompat
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.faldez.shachi.data.PostRepository.Companion.STARTING_PAGE_INDEX
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.response.GelbooruPost
import com.faldez.shachi.model.response.GelbooruPostResponse
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService
import retrofit2.HttpException
import java.io.IOException
import java.time.format.DateTimeFormatter

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
        val position = params.key ?: STARTING_PAGE_INDEX
        try {
            val posts = when (action.server?.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(position).toString()
                    Log.d("PostPagingSource", url)
                    service.gelbooru.getPosts(url).applyBlacklist(action.server.blacklistedTags)
                        ?.mapToPost(action.server.serverId) ?: listOf()
                }
                ServerType.Danbooru -> {
                    TODO("not yet implemented")
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
                prevKey = if (position == STARTING_PAGE_INDEX) null else position,
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

    private fun List<GelbooruPost>.mapToPost(serverId: Int): List<Post> {
        return this.map { post ->
            Post(
                height = post.height,
                width = post.width,
                score = post.score,
                fileUrl = post.fileUrl,
                parentId = post.parentId,
                sampleUrl = post.sampleUrl,
                sampleWidth = post.sampleWidth,
                sampleHeight = post.sampleHeight,
                previewUrl = post.previewUrl,
                previewWidth = post.previewWidth,
                previewHeight = post.previewHeight,
                rating = post.rating,
                tags = post.tags,
                postId = post.id,
                serverId = serverId,
                change = post.change,
                md5 = post.md5,
                creatorId = post.creatorId,
                hasChildren = post.hasChildren,
                createdAt = post.createdAt?.format(DateTimeFormatter.ofPattern("cccc, dd MMMM y hh:mm:ss a",
                    LocaleListCompat.getDefault().get(0))),
                status = post.status,
                source = post.source,
                hasNotes = post.hasNotes,
                hasComments = post.hasComments,
            )
        }
    }
}