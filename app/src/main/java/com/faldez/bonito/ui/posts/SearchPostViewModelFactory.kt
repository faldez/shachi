package com.faldez.bonito.ui.posts

import com.faldez.bonito.data.GelbooruRepository
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import java.lang.IllegalArgumentException

class SearchPostViewModelFactory constructor(
    private val repository: GelbooruRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(PostsViewModel::class.java)) {
            PostsViewModel(this.repository, handle) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}