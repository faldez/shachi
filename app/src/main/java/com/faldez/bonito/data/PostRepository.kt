package com.faldez.bonito.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.bonito.model.Post
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.response.GelbooruPostResponse
import com.faldez.bonito.service.Action
import com.faldez.bonito.service.BooruService
import com.faldez.bonito.service.GelbooruService
import kotlinx.coroutines.flow.Flow

class PostRepository constructor(private val service: BooruService) {
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

    companion object {
        const val STARTING_PAGE_INDEX = 0
        const val NETWORK_PAGE_SIZE = 50
    }
}