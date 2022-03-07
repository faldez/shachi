package com.faldez.shachi.ui.more

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.shachi.data.repository.*

class MoreViewModelFactory constructor(
    private val serverRepository: ServerRepository,
    private val blacklistTagRepository: BlacklistTagRepository,
    private val savedSearchRepository: SavedSearchRepository,
    private val favoriteRepository: FavoriteRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val tagRepository: TagRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(MoreViewModel::class.java)) {
            MoreViewModel(
                serverRepository,
                blacklistTagRepository,
                savedSearchRepository,
                favoriteRepository,
                searchHistoryRepository,
                tagRepository
            ) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}