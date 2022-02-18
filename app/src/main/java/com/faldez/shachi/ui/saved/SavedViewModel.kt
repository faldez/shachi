package com.faldez.shachi.ui.saved

import android.util.Log
import android.util.SparseIntArray
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.repository.FavoriteRepository
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class SavedViewModel(
    private val savedSearchRepository: SavedSearchRepository,
    private val postRepository: PostRepository,
    private val favoriteRepository: FavoriteRepository,
) : ViewModel() {
    val state: Flow<PagingData<SavedSearchPost>>
    private val stateMap: MutableStateFlow<MutableMap<Int, Flow<PagingData<SavedSearchPost>>>> =
        MutableStateFlow(
            mutableMapOf())
    val scrollState: MutableStateFlow<SparseIntArray> = MutableStateFlow(SparseIntArray())
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        val getSavedSearchFlow = actionStateFlow.filterIsInstance<UiAction.GetSavedSearch>()
            .onStart { emit(UiAction.GetSavedSearch()) }
        val savedSearchFlow = savedSearchRepository.getAll().distinctUntilChanged()

        state = combine(getSavedSearchFlow, savedSearchFlow, stateMap, ::Triple)
            .map { (action, data, map) ->
                Log.d("SavedViewModel", "collect savedSearches")
                val list = data.map { savedSearch ->
                    var posts =
                        if (!action.clearAll) map[savedSearch.savedSearch.savedSearchId] else null
                    if (posts == null) {
                        posts = getSearchPosts(savedSearch).cachedIn(viewModelScope)
                        map[savedSearch.savedSearch.savedSearchId] = posts
                    }
                    SavedSearchPost(savedSearch = savedSearch, posts = posts)
                }
                PagingData.from(list)
            }.cachedIn(viewModelScope)

        accept = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
            }
        }
    }

    private fun getSearchPosts(savedSearch: SavedSearchServer): Flow<PagingData<SavedSearchPost>> {
        return postRepository.getSearchPostsResultStream(Action.SearchPost(server = savedSearch.server,
            tags = savedSearch.savedSearch.tags)).map { pagingData ->
            pagingData.map { post ->
                val postId =
                    favoriteRepository.queryByServerUrlAndPostId(post.serverId,
                        post.postId)
                post.favorite = postId != null
                SavedSearchPost(post = post)
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

    fun posts(savedSearchId: Int): Flow<PagingData<Post>?> =
        stateMap.value[savedSearchId]!!.map { it.map { item -> item.post!! } }

    fun delete(savedSearch: SavedSearch) = viewModelScope.launch {
        savedSearchRepository.delete(savedSearch)
    }

    fun putScroll(position: Int, scroll: Int) {
        scrollState.getAndUpdate {
            it.put(position, scroll)
            it
        }
    }
}

data class SavedSearchPost(
    val savedSearch: SavedSearchServer? = null,
    val post: Post? = null,
    val scroll: Int? = null,
    val posts: Flow<PagingData<SavedSearchPost>?> = flowOf(null),
)

data class UiState(
    val posts: Map<SavedSearch, List<Post>?>? = null,
)

sealed class UiAction {
    data class GetSavedSearch(val clearAll: Boolean = false) : UiAction()
}