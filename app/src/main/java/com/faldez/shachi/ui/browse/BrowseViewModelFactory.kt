package com.faldez.shachi.ui.browse

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.shachi.data.repository.*
import com.faldez.shachi.data.repository.favorite.FavoriteRepository
import com.faldez.shachi.data.repository.post.PostRepository
import com.faldez.shachi.data.repository.saved_search.SavedSearchRepository
import com.faldez.shachi.data.repository.search_history.SearchHistoryRepository

class BrowseViewModelFactory constructor(
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedSearchRepository: SavedSearchRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
            BrowseViewModel(
                postRepository,
                serverRepository,
                favoriteRepository,
                savedSearchRepository,
                searchHistoryRepository,
                handle) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}