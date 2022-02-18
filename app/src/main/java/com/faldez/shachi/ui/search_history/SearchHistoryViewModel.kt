package com.faldez.shachi.ui.search_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.faldez.shachi.model.SearchHistoryServer
import com.faldez.shachi.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

class SearchHistoryViewModel(
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {
    val state: Flow<PagingData<SearchHistoryServer>>

    init {
        state = searchHistoryRepository.getAll()
            .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)
    }
}