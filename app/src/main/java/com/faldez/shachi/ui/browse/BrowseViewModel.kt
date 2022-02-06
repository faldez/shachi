package com.faldez.shachi.ui.browse

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.faldez.shachi.model.*
import com.faldez.shachi.repository.FavoriteRepository
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BrowseViewModel constructor(
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedSearchRepository: SavedSearchRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val state: StateFlow<UiState>
    val pagingDataFlow: Flow<PagingData<Post>>
    val accept: (UiAction) -> Unit

    init {
        Log.d("PostsViewModel",
            "init " + savedStateHandle.get(LAST_SEARCH_TAGS) + " " + savedStateHandle.get(
                LAST_TAGS_SCROLLED))
        val initialTags: List<TagDetail> =
            savedStateHandle.get(LAST_SEARCH_TAGS)
                ?: listOf()
        val lastTagsScrolled: List<TagDetail> = savedStateHandle.get(LAST_TAGS_SCROLLED) ?: listOf()
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches = actionStateFlow.filterIsInstance<UiAction.Search>().distinctUntilChanged()
        val tagsScrolled =
            actionStateFlow.filterIsInstance<UiAction.Scroll>().distinctUntilChanged()
                .onStart {
                    emit(UiAction.Scroll(null, currentTags = lastTagsScrolled))
                }
                .shareIn(scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    replay = 1)

        val getServer =
            actionStateFlow.filterIsInstance<UiAction.GetSelectedOrSelectServer>()
                .distinctUntilChanged()
                .onStart { emit(UiAction.GetSelectedOrSelectServer()) }
                .flatMapLatest {
                    if (it.server == null) {
                        getSelectedServer()
                    } else {
                        selectServer(it.server)
                    }
                }

        state =
            combine(getServer, searches, tagsScrolled, ::Triple).map { (server, search, scroll) ->
                Log.d("BrowseViewModel",
                    "$server ${search.tags} != ${scroll.currentTags}) || (${search.serverUrl ?: scroll.currentServerUrl} != ${scroll.currentServerUrl})")
                UiState(
                    server = server,
                    tags = search.tags,
                    lastTagsScrolled = scroll.currentTags,
                    hasNotScrolledForCurrentTag = (search.tags != scroll.currentTags) || (search.serverUrl ?: scroll.currentServerUrl != scroll.currentServerUrl)
                )
            }.stateIn(scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        val serverChange = state.map { it.server }.distinctUntilChanged()
        pagingDataFlow = combine(serverChange, searches, ::Pair).flatMapLatest { (server, search) ->
            Log.d("BrowseViewModel", "pagingDataFlow $server $searches")
            searchPosts(server, tags = search.tags.toQuery()).map {
                it.map { post ->
                    val postId =
                        favoriteRepository.queryByServerUrlAndPostId(post.serverId, post.postId)
                    post.favorite = postId != null
                    post
                }
            }
        }.cachedIn(viewModelScope)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    private fun getSelectedServer(): Flow<ServerView?> {
        return serverRepository.getSelectedServer()
    }

    private fun selectServer(server: Server) = serverRepository.getServer(server.serverId)

    private fun searchPosts(server: ServerView?, tags: String): Flow<PagingData<Post>> {
        val action = Action.SearchPost(server, tags)
        return postRepository.getSearchPostsResultStream(action)
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

    fun saveSearch(title: String?) {
        viewModelScope.launch {
            state.value.server?.let { server ->
                savedSearchRepository.insert(SavedSearch(serverId = server.serverId,
                    tags = state.value.tags.toQuery(),
                    savedSearchTitle = title ?: state.value.tags.first().name))
            }
        }
    }

    override fun onCleared() {
        savedStateHandle.set(LAST_SEARCH_TAGS, state.value.tags)
        savedStateHandle.set(LAST_TAGS_SCROLLED, state.value.lastTagsScrolled)
        super.onCleared()
        Log.d("PostsViewModel",
            "onCleared " + state.value.tags + " " + state.value.lastTagsScrolled)
    }

    private fun List<TagDetail>.toQuery(): String {
        return this.joinToString(" ") {
            if (it.excluded) {
                "-${it.name}"
            } else {
                it.name
            }
        }
    }
}

sealed class UiAction {
    data class Search(val serverUrl: String?, val tags: List<TagDetail>) : UiAction()
    data class Scroll(
        val currentServerUrl: String?,
        val currentTags: List<TagDetail>,
    ) : UiAction()

    data class GetSelectedOrSelectServer(val server: Server? = null) : UiAction()
}

data class UiState(
    val server: ServerView? = null,
    val tags: List<TagDetail> = listOf(),
    val lastTagsScrolled: List<TagDetail> = listOf(),
    val hasNotScrolledForCurrentTag: Boolean = false,
)

private const val LAST_SEARCH_TAGS: String = "last_search_tags"
private const val LAST_TAGS_SCROLLED: String = "last_tags_scrolled"