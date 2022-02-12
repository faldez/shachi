package com.faldez.shachi.ui.saved

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SavedViewModel(
    private val savedSearchRepository: SavedSearchRepository,
    private val postRepository: PostRepository,
) : ViewModel() {
    val savedSearchesFlow: Flow<List<SavedSearchServer>?>
    val state: Flow<PagingData<SavedSearchPost>>
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        savedSearchesFlow = actionStateFlow.filterIsInstance<UiAction.GetSavedSearchFlow>()
            .onStart { emit(UiAction.GetSavedSearchFlow) }
            .flatMapLatest { savedSearchRepository.getAll() }

        val getSavedSearches =
            actionStateFlow.filterIsInstance<UiAction.GetSavedSearch>()
                .flatMapLatest {
                    savedSearchRepository.getSavedSearchesStream()
                }

        state =
            getSavedSearches.filter { it != null }
                .map { data ->
                    data!!.map { savedSearch ->
                        val posts =
                            postRepository.getSearchPostsResultStream(Action.SearchPost(server = savedSearch.server,
                                tags = savedSearch.savedSearch.tags)).map { pagingData ->
                                pagingData.map {
                                    SavedSearchPost(post = it)
                                }
                            }.cachedIn(viewModelScope)
                        SavedSearchPost(savedSearch = savedSearch, posts = posts)
                    }
                }.cachedIn(viewModelScope)

        accept = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
            }
        }

        viewModelScope.launch {
            savedSearchesFlow.collectLatest {
                Log.d("SavedViewModel", "collect savedSearchesFlow")
                accept(UiAction.GetSavedSearch)
            }
        }
    }

    fun delete(savedSearch: SavedSearch) = viewModelScope.launch {
        savedSearchRepository.delete(savedSearch)
//        accept(UiAction.GetSavedSearch)
    }
}

data class SavedSearchPost(
    val savedSearch: SavedSearchServer? = null,
    val post: Post? = null,
    val posts: Flow<PagingData<SavedSearchPost>?> = flowOf(null),
)

data class UiState(
    val posts: Map<SavedSearch, List<Post>?>? = null,
)

sealed class UiAction {
    object GetSavedSearch : UiAction()
    object GetSavedSearchFlow: UiAction()
    data class GetSavedSearchPost(val savedSearch: SavedSearch) : UiAction()
}