package com.faldez.bonito.data

import android.util.Log
import androidx.core.os.LocaleListCompat
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.faldez.bonito.data.PostRepository.Companion.STARTING_PAGE_INDEX
import com.faldez.bonito.model.Post
import com.faldez.bonito.model.ServerType
import com.faldez.bonito.model.response.GelbooruPostResponse
import com.faldez.bonito.service.Action
import com.faldez.bonito.service.BooruService
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
        return try {
            val posts = when (action.server?.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(position).toString()
                    Log.d("PostPagingSource", url)
                    service.gelbooru.getPosts(url).mapToPost(action.server.url) ?: listOf()
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

    private fun GelbooruPostResponse.mapToPost(serverUrl: String): List<Post>? {
        return this.posts.post?.map { post ->
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
                serverUrl = serverUrl,
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