package com.faldez.shachi.ui.saved

import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.repository.FavoriteRepository
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import kotlin.collections.set

class SavedViewModel(
    private val savedSearchRepository: SavedSearchRepository,
    private val postRepository: PostRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val savedSearchFlow: Flow<List<SavedSearchPost>>
    private val postsMap: MutableMap<Int, Flow<PagingData<Post>>> =
        mutableMapOf()
    val scrollState: MutableStateFlow<SparseArray<Int>> =
        savedStateHandle[LAST_SCROLL_POSITIONS] ?: MutableStateFlow(SparseArray<Int>())
    val accept: (UiAction) -> Unit

    init {
        val actionSharedFlow = MutableSharedFlow<UiAction>()
        val configFlow =
            actionSharedFlow.filterIsInstance<UiAction.SetConfig>().distinctUntilChanged()
                .map { it.config }

        val savedSearch = savedSearchRepository.getAllFlow().distinctUntilChanged()
        savedSearchFlow = combine(configFlow, savedSearch, ::Pair)
            .map { (config, data) ->
                Log.d("SavedViewModel", "collect savedSearches")
                data.map { savedSearch ->
                    var posts = postsMap[savedSearch.savedSearch.savedSearchId]
                    if (posts == null) {
                        posts = getSearchPosts(savedSearch, config).cachedIn(viewModelScope)
                        postsMap[savedSearch.savedSearch.savedSearchId] = posts
                    }
                    SavedSearchPost(savedSearch = savedSearch, posts = posts)
                }
            }.shareIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                replay = 1
            )

        accept = { action ->
            viewModelScope.launch {
                actionSharedFlow.emit(action)
            }
        }
    }

    private fun getSearchPosts(
        savedSearch: SavedSearchServer,
        config: Config,
    ): Flow<PagingData<Post>> {
        return postRepository.getSearchPostsResultStream(Action.SearchPost(server = savedSearch.server,
            tags = savedSearch.savedSearch.tags, limit = 10)).map { pagingData ->
            pagingData.filter { item ->
                when (item.rating) {
                    Rating.Questionable -> config.questionableFilter != "mute"
                    Rating.Explicit -> config.explicitFilter != "mute"
                    Rating.Safe -> true
                }
            }.map { post ->
                val postId =
                    favoriteRepository.queryByServerUrlAndPostId(post.serverId,
                        post.postId)
                post.favorite = postId != null
                post
            }
        }
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

    fun posts(savedSearchId: Int): Flow<PagingData<Post>?> = savedSearchFlow.flatMapLatest { list ->
        list.find { it.savedSearch.savedSearch.savedSearchId == savedSearchId }?.posts!!
    }

    fun delete(savedSearch: SavedSearch) = viewModelScope.launch {
        savedSearchRepository.delete(savedSearch)
    }

    fun putScroll(position: Int, scroll: Int) {
        scrollState.value.put(position, scroll)
    }

    fun saveSearch(savedSearch: SavedSearch) {
        viewModelScope.launch {
            savedSearchRepository.insert(savedSearch)
        }
    }

    override fun onCleared() {
        savedStateHandle[LAST_SCROLL_POSITIONS] = scrollState.value
        super.onCleared()
    }
}

data class SavedSearchPost(
    val savedSearch: SavedSearchServer,
    val posts: Flow<PagingData<Post>?>,
)

data class Config(
    val questionableFilter: String = "disable",
    val explicitFilter: String = "disable",
)

sealed class UiAction {
    data class SetConfig(val config: Config) : UiAction()
}

private const val LAST_SCROLL_POSITIONS: String = "last_scroll_positions"
