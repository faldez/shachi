package com.faldez.shachi.ui.browse

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.faldez.shachi.data.api.Action
import com.faldez.shachi.data.model.*
import com.faldez.shachi.data.preference.Filter
import com.faldez.shachi.data.repository.ServerRepository
import com.faldez.shachi.data.repository.favorite.FavoriteRepository
import com.faldez.shachi.data.repository.post.PostRepository
import com.faldez.shachi.data.repository.saved_search.SavedSearchRepository
import com.faldez.shachi.data.repository.search_history.SearchHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime

class BrowseViewModel constructor(
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedSearchRepository: SavedSearchRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val state: StateFlow<UiState>
    val pagingDataFlow: Flow<PagingData<Post>>
    val accept: (UiAction) -> Unit

    init {
        Log.d("PostsViewModel",
            "init " + savedStateHandle.get(LAST_SEARCH_TAGS) + " " + savedStateHandle.get(
                LAST_TAGS_SCROLLED))
        val initialTags: String =
            savedStateHandle.get(LAST_SEARCH_TAGS)
                ?: ""
        val lastTagsScrolled: String = savedStateHandle.get(LAST_TAGS_SCROLLED) ?: ""
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val searches =
            actionStateFlow.filterIsInstance<UiAction.Search>().distinctUntilChanged()
//        val tagsScrolled =
//            actionStateFlow.filterIsInstance<UiAction.Scroll>().distinctUntilChanged()
//                .onStart {
//                    emit(UiAction.Scroll(null, currentTags = lastTagsScrolled))
//                }
//                .shareIn(scope = viewModelScope,
//                    started = SharingStarted.Eagerly,
//                    replay = 1)

        val getServer = getSelectedServer()

        state =
            combine(getServer, searches, ::Pair).map { (server, search) ->
                UiState(
                    server = server,
                    tags = search.tags,
                    questionableFilter = search.questionableFilter,
                    explicitFilter = search.explicitFilter,
                    start = search.start
                )
            }.stateIn(scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = UiState()
            )

        pagingDataFlow =
            state.filter { it.server != null }
                .flatMapLatest {
                    searchPosts(it.server!!, tags = it.tags, it.start).map { data ->
                        data.applyFilters(it.server.blacklistedTags,
                            it.questionableFilter == Filter.Mute,
                            it.explicitFilter == Filter.Mute)
                            .map { post ->
                                val postId =
                                    favoriteRepository.queryByServerUrlAndPostId(post.serverId,
                                        post.postId)
                                post.favorite = postId != null
                                post
                            }
                    }
                }.cachedIn(viewModelScope)

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                state.collect {
                    if (it.server != null && it.tags.isNotEmpty()) {
                        val tags = it.tags
                        Log.d("BrowseViewModel",
                            "insert ${it.server.serverId} $tags to history")
                        insertTagsTagsToHistory(it.server.serverId, tags)
                    }
                }
            }
        }
    }

    private fun getSelectedServer(): Flow<ServerView?> {
        return serverRepository.getSelectedServer()
    }

    fun selectServer(server: ServerView) {
        CoroutineScope(Dispatchers.IO).launch {
            serverRepository.setSelectedServer(server.serverId)
        }
    }


    private fun searchPosts(server: ServerView, tags: String, start: Int?): Flow<PagingData<Post>> {
        Log.d("BrowseViewModel", "searchPost(server=$server, tags=$tags, start=$start)")
        val action = Action.SearchPost(server = server, tags = tags, start = start)
        return postRepository.getSearchPostsResultStream(action)
    }

    fun favoritePost(favorite: Post) {
        viewModelScope.launch {
            favoriteRepository.insert(favorite.copy(dateAdded = ZonedDateTime.now().toInstant()
                .toEpochMilli()))
        }
    }

    fun deleteFavoritePost(favorite: Post) {
        viewModelScope.launch {
            favoriteRepository.delete(favorite)
        }
    }

    fun saveSearch(title: String?, tags: String) {
        viewModelScope.launch {
            state.value.server?.let { server ->
                savedSearchRepository.insert(serverId = server.serverId,
                    tags = tags,
                    title = title ?: state.value.tags.split(" ").first())
            }
        }
    }

    private fun insertTagsTagsToHistory(serverId: Int, tags: String) =
        CoroutineScope(Dispatchers.IO).launch {
            searchHistoryRepository.insert(SearchHistory(tags = tags,
                createdAt = ZonedDateTime.now().toInstant().toEpochMilli(),
                serverId = serverId))
        }

    override fun onCleared() {
        savedStateHandle.set(LAST_SEARCH_SERVER, state.value.server)
        savedStateHandle.set(LAST_SEARCH_TAGS, state.value.tags)
        super.onCleared()
    }

    private fun List<TagDetail>.toQuery(): String {
        return this.joinToString(" ") { it.toString() }
    }
}

sealed class UiAction {
    data class Search(
        val tags: String,
        val questionableFilter: Filter,
        val explicitFilter: Filter,
        val start: Int? = null,
    ) : UiAction()

    data class Scroll(
        val currentServerUrl: String?,
        val currentTags: String,
    ) : UiAction()
}

data class UiState(
    val server: ServerView? = null,
    val tags: String = "",
    val questionableFilter: Filter = Filter.Disable,
    val explicitFilter: Filter = Filter.Disable,
    val start: Int? = null,
)

private const val LAST_SEARCH_SERVER: String = "last_search_server"
private const val LAST_SEARCH_TAGS: String = "last_search_tags"
private const val LAST_TAGS_SCROLLED: String = "last_tags_scrolled"