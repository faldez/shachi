package com.faldez.shachi.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.response.GelbooruPostResponse
import com.faldez.shachi.service.Action
import com.faldez.shachi.service.BooruService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

    suspend fun getSavedSearchPosts(action: Action.SearchSavedSearchPost): Pair<SavedSearch, List<Post>?> {
        try {
            when (action.savedSearch.server.type) {
                ServerType.Gelbooru -> {
                    val url = action.buildGelbooruUrl(0, 50).toString()

                    Log.d("PostPagingSource", url)

                    val posts =
                        service.gelbooru.getPosts(url).mapToPost(action.savedSearch.server.url)

                    return Pair(action.savedSearch, posts)
                }
                ServerType.Danbooru -> {
                    TODO("not yet implemented")
                }
                null -> {
                    throw Error("server not found")
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
                    Log.d("PostPagingSource", url)
                    if (service.gelbooru.getPosts(url).mapToPost(action.server.url)
                            .isNullOrEmpty()
                    ) {
                        throw Error("list empty")
                    }
                }
                ServerType.Danbooru -> {
                    TODO("not yet implemented")
                }
                null -> {
                    throw Error("server not found")
                }
            }
        } catch (exception: IOException) {
            throw Error(exception)
        } catch (exception: HttpException) {
            throw Error(exception)
        }
    }

    private fun GelbooruPostResponse.mapToPost(serverUrl: String): List<Post>? {
        return this.posts?.post?.map { post ->
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
                createdAt = post.createdAt?.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)),
                status = post.status,
                source = post.source,
                hasNotes = post.hasNotes,
                hasComments = post.hasComments,
            )
        }
    }

    companion object {
        const val STARTING_PAGE_INDEX = 0
        const val NETWORK_PAGE_SIZE = 50
    }
}