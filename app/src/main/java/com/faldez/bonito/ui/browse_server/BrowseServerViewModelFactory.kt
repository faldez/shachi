package com.faldez.bonito.ui.browse_server

import com.faldez.bonito.data.PostRepository
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.bonito.data.FavoriteRepository
import com.faldez.bonito.data.ServerRepository
import java.lang.IllegalArgumentException

class BrowseServerViewModelFactory constructor(
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val favoriteRepository: FavoriteRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(BrowseServerViewModel::class.java)) {
            BrowseServerViewModel(this.postRepository, this.serverRepository, favoriteRepository, handle) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}