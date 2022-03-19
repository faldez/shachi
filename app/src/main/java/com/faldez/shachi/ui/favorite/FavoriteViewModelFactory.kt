package com.faldez.shachi.ui.favorite

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.shachi.data.repository.favorite.FavoriteRepository

class FavoriteViewModelFactory(
    private val favoriteRepository: FavoriteRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            return FavoriteViewModel(
                this.favoriteRepository,
                handle) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }
}