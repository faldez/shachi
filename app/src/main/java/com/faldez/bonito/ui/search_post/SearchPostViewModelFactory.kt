package com.faldez.bonito.ui.search_post

import com.faldez.bonito.data.PostRepository
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import java.lang.IllegalArgumentException

class SearchPostViewModelFactory constructor(
    private val postRepository: PostRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(SearchPostViewModel::class.java)) {
            SearchPostViewModel(this.postRepository, handle) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}