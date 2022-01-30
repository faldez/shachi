package com.faldez.sachi.ui.browse

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.sachi.data.FavoriteRepository
import com.faldez.sachi.data.PostRepository
import com.faldez.sachi.data.SavedSearchRepository
import com.faldez.sachi.data.ServerRepository
import com.faldez.sachi.model.Server

class BrowseViewModelFactory constructor(
    private val server: Server?,
    private val tags: String?,
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedSearchRepository: SavedSearchRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
            BrowseViewModel(
                this.server,
                this.tags,
                this.postRepository,
                this.serverRepository,
                favoriteRepository,
                this.savedSearchRepository,
                handle) as T
        } else if (modelClass.isAssignableFrom(SavedSearchBrowseViewModel::class.java)) {
            SavedSearchBrowseViewModel(
                this.server,
                this.tags,
                this.postRepository,
                this.serverRepository,
                favoriteRepository,
                this.savedSearchRepository,
                handle) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}