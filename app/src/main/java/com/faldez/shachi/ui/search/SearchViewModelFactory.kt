package com.faldez.shachi.ui.search

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.repository.TagRepository

class SearchViewModelFactory constructor(
    private val server: ServerView,
    private val tagRepository: TagRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(SearchSimpleViewModel::class.java)) {
            SearchSimpleViewModel(server, tagRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}