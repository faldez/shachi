package com.faldez.shachi.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.SavedSearchServer
import retrofit2.HttpException
import java.io.IOException

class SavedSearchPagingSource(private val db: AppDatabase) :
    PagingSource<Int, SavedSearchServer>() {
    override fun getRefreshKey(state: PagingState<Int, SavedSearchServer>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1)
                ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SavedSearchServer> {
        return try {
            val savedSearches = db.savedSearchDao().getAll()
            LoadResult.Page(data = savedSearches ?: listOf(),
                prevKey = null,
                nextKey = null
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}