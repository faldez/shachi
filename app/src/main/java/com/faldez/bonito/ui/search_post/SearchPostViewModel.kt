package com.faldez.bonito.ui.search_post

import android.content.SharedPreferences
import android.util.Log
import com.faldez.bonito.data.PostRepository
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.faldez.bonito.data.FavoriteRepository
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.model.*
import com.faldez.bonito.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchPostViewModel constructor(
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val state: StateFlow<UiState>
    val pagingDataFlow: Flow<PagingData<Post>>
    val accept: (UiAction) -> Unit

    init {
        Log.d("PostsViewModel",
            "init " + savedStateHandle.get(LAST_SEARCH_TAGS) + " " + savedStateHandle.get(
                LAST_TAGS_SCROLLED))
        val initialTags: String = savedStateHandle.get(LAST_SEARCH_TAGS) ?: ""
        val lastTagsScrolled: String = savedStateHandle.get(LAST_TAGS_SCROLLED) ?: ""
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow.filterIsInstance<UiAction.Search>().distinctUntilChanged()
            .onStart { emit(UiAction.Search(null, tags = initialTags)) }
        val tagsScrolled =
            actionStateFlow.filterIsInstance<UiAction.Scroll>().distinctUntilChanged()
                .shareIn(scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    replay = 1)
                .onStart {
                    emit(UiAction.Scroll(null, currentTags = lastTagsScrolled))
                }

        val getServer =
            actionStateFlow.filterIsInstance<UiAction.GetSelectedServer>().distinctUntilChanged()
                .onStart { emit(UiAction.GetSelectedServer) }
                .flatMapLatest { getSelectedServer() }

        state =
            combine(getServer, searches, tagsScrolled, ::Triple).map { (server, search, scroll) ->
                UiState(
                    server = server?.let {
                        Server(title = it.title,
                            url = it.url,
                            type = it.type)
                    },
                    tags = search.tags,
                    lastTagsScrolled = scroll.currentTags,
                    hasNotScrolledForCurrentTag = (search.tags != scroll.currentTags) || (search.serverUrl != scroll.currentServerUrl)
                )
            }.stateIn(scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        val serverChange = state.map { it.server }.distinctUntilChanged()
        pagingDataFlow = combine(serverChange, searches, ::Pair).flatMapLatest { (server, search) ->
            searchPosts(server, tags = search.tags).map {
                it.map { post ->
                    val postId =
                        favoriteRepository.queryByServerUrlAndPostId(post.serverUrl, post.postId)
                    Log.d("SearchPostViewModel", "$postId")
                    post.copy(favorite = postId != null)
                }
            }
        }.cachedIn(viewModelScope)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getSelectedServer(): Flow<ServerWithSelected?> {
        return serverRepository.getSelectedServer()
    }

    private fun searchPosts(server: Server?, tags: String): Flow<PagingData<Post>> {
        val action = Action.SearchPost(server, tags)
        return postRepository.getSearchPostsResultStream(action)
    }

    fun favoritePost(favorite: Favorite) {
        viewModelScope.launch {
            favoriteRepository.insert(favorite)
        }
    }

    fun deleteFavoritePost(favorite: Favorite) {
        viewModelScope.launch {
            favoriteRepository.delete(favorite)
        }
    }

    override fun onCleared() {
        savedStateHandle.set(LAST_SEARCH_TAGS, state.value.tags)
        savedStateHandle.set(LAST_TAGS_SCROLLED, state.value.lastTagsScrolled)
        super.onCleared()
        Log.d("PostsViewModel",
            "onCleared " + state.value.tags + " " + state.value.lastTagsScrolled)
    }
}

sealed class UiAction {
    data class Search(val serverUrl: String?, val tags: String) : UiAction()
    data class Scroll(
        val currentServerUrl: String?,
        val currentTags: String,
    ) : UiAction()

    object GetSelectedServer : UiAction()
    data class FavoritePost(val favorite: Favorite) : UiAction()
    data class DeleteFavoritePost(val favorite: Favorite) : UiAction()
    data class GetFavoritePost(val serverUrl: String, val postIds: List<Int>) : UiAction()
}

data class UiState(
    val server: Server? = null,
    val tags: String = "",
    val lastTagsScrolled: String = "",
    val hasNotScrolledForCurrentTag: Boolean = false,
)

private const val LAST_SEARCH_TAGS: String = "last_search_tags"
private const val LAST_TAGS_SCROLLED: String = "last_tags_scrolled"