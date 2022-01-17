package com.faldez.bonito.ui.posts

import com.faldez.bonito.data.GelbooruRepository
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.faldez.bonito.model.Post
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostsViewModel constructor(
    private val repository: GelbooruRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val state: StateFlow<UiState>
    val pagingDataFlow: Flow<PagingData<Post>>
    val accept: (UiAction) -> Unit

    init {
        val initialTags: String = savedStateHandle.get(LAST_SEARCH_TAGS) ?: ""
        val lastTagsScrolled: String = savedStateHandle.get(LAST_TAGS_SCROLLED) ?: ""
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow.filterIsInstance<UiAction.Search>().distinctUntilChanged()
            .onStart { emit(UiAction.Search(tags = initialTags)) }
        val tagsScrolled =
            actionStateFlow.filterIsInstance<UiAction.Scroll>().distinctUntilChanged()
                .shareIn(scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    replay = 1)
                .onStart {
                    emit(UiAction.Scroll(currentTags = lastTagsScrolled))
                }
        pagingDataFlow =
            searches.flatMapLatest { searchPosts(tags = it.tags) }.cachedIn(viewModelScope)

        state = combine(searches, tagsScrolled, ::Pair).map { (search, scroll) ->
            UiState(
                tags = search.tags,
                lastTagsScrolled = scroll.currentTags,
                hasNotScrolledForCurrentTag = search.tags != scroll.currentTags
            )
        }.stateIn(scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = UiState()
        )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun searchPosts(tags: String): Flow<PagingData<Post>> {
        return repository.getSearchPostsResultStream(tags)
    }


    override fun onCleared() {
        savedStateHandle[LAST_SEARCH_TAGS] = state.value.tags
        savedStateHandle[LAST_TAGS_SCROLLED] = state.value.lastTagsScrolled
        super.onCleared()
    }
}

sealed class UiAction {
    data class Search(val tags: String) : UiAction()
    data class Scroll(
        val currentTags: String,
    ) : UiAction()
}

data class UiState(
    val tags: String = "",
    val lastTagsScrolled: String = "",
    val hasNotScrolledForCurrentTag: Boolean = false,
)

private const val LAST_SEARCH_TAGS: String = "last_search_tags"
private const val LAST_TAGS_SCROLLED: String = "last_tags_scrolled"