package com.faldez.shachi.ui.search_simple

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.shachi.repository.TagRepository

class SearchSimpleViewModelFactory constructor(
    private val tagRepository: TagRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(SearchSimpleViewModel::class.java)) {
            SearchSimpleViewModel(this.tagRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}