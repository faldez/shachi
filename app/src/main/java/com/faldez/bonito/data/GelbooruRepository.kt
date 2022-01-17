package com.faldez.bonito.data
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.faldez.bonito.model.Post
import com.faldez.bonito.service.GelbooruService
import kotlinx.coroutines.flow.Flow

class GelbooruRepository constructor(private val service: GelbooruService): ViewModel() {
    fun getSearchPostsResultStream(tags: String) : Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                GelbooruPagingSource(service, tags)
            }
        ).flow
    }
//    fun getTags(pattern: String) = service.getTags(pattern)

    companion object {
        const val NETWORK_PAGE_SIZE = 100
    }
}