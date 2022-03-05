package com.faldez.shachi.ui.search_history

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.shachi.data.repository.SearchHistoryRepository

class SearchHistoryViewModelFactory constructor(
    private val searchHistoryRepository: SearchHistoryRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(SearchHistoryViewModel::class.java)) {
            SearchHistoryViewModel(searchHistoryRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}