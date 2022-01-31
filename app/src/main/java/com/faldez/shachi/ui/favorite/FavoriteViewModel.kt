package com.faldez.shachi.ui.favorite

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.faldez.shachi.data.FavoriteRepository
import com.faldez.shachi.model.Post
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val state: StateFlow<UiState>
    val pagingDataFlow: Flow<PagingData<Post>>
    val accept: (UiAction) -> Unit

    init {
        val initialTags: String =
            savedStateHandle.get(LAST_SEARCH_TAGS)
                ?: ""
        val lastTagsScrolled: String = savedStateHandle.get(LAST_TAGS_SCROLLED) ?: ""

        val actionStateFlow = MutableSharedFlow<UiAction>()
        val tagsScrolled =
            actionStateFlow.filterIsInstance<UiAction.Scroll>()
                .distinctUntilChanged()
                .onStart {
                    emit(UiAction.Scroll(currentTags = lastTagsScrolled))
                }

        val searches = actionStateFlow.filterIsInstance<UiAction.SearchFavorite>()

        pagingDataFlow = searches.onStart { emit(UiAction.SearchFavorite(initialTags)) }
            .flatMapLatest { favoriteRepository.query(it.tags) }
            .cachedIn(viewModelScope)

        state =
            combine(searches, tagsScrolled, ::Pair).map { (search, scroll) ->
                UiState(
                    tags = search.tags,
                    lastTagsScrolled = scroll.currentTags,
                    hasNotScrolledForCurrentTag = search.tags != scroll.currentTags
                )
            }.stateIn(scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        accept = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
            }
        }
    }


    fun favoritePost(favorite: Post) {
        viewModelScope.launch {
            favoriteRepository.insert(favorite)
        }
    }

    fun deleteFavoritePost(favorite: Post) {
        viewModelScope.launch {
            favoriteRepository.delete(favorite)
        }
    }
}

sealed class UiAction {
    data class SearchFavorite(val tags: String) : UiAction()
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