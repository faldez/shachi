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
    val state: StateFlow<List<SavedSearchPost>>
    private val postsMap: MutableMap<Int, Flow<PagingData<Post>>> =
        mutableMapOf()
    val scrollState: MutableStateFlow<SparseIntArray> = MutableStateFlow(SparseIntArray())

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        val getSavedSearchFlow = actionStateFlow.filterIsInstance<UiAction.GetSavedSearch>()
            .onStart { emit(UiAction.GetSavedSearch()) }
        val savedSearchFlow = savedSearchRepository.getAllFlow().distinctUntilChanged()

        state = combine(getSavedSearchFlow, savedSearchFlow, ::Pair)
            .map { (action, data) ->
                Log.d("SavedViewModel", "collect savedSearches")
                data.map { savedSearch ->
                    var posts =
                        if (!action.clearAll) postsMap[savedSearch.savedSearch.savedSearchId] else null
                    if (posts == null) {
                        posts = getSearchPosts(savedSearch).cachedIn(viewModelScope)
                        postsMap[savedSearch.savedSearch.savedSearchId] = posts
                    }
                    SavedSearchPost(savedSearch = savedSearch, posts = posts)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = listOf()
            )
    }

    private fun getSearchPosts(savedSearch: SavedSearchServer): Flow<PagingData<Post>> {
        return postRepository.getSearchPostsResultStream(Action.SearchPost(server = savedSearch.server,
            tags = savedSearch.savedSearch.tags, limit = 10)).map { pagingData ->
            pagingData.map { post ->
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

    fun posts(savedSearchId: Int): Flow<PagingData<Post>?> = postsMap[savedSearchId]!!

    fun delete(savedSearch: SavedSearch) = viewModelScope.launch {
        savedSearchRepository.delete(savedSearch)
    }

    fun putScroll(position: Int, scroll: Int) {
        scrollState.getAndUpdate {
            it.put(position, scroll)
            it
        }
    }

    fun saveSearch(savedSearch: SavedSearch) {
        viewModelScope.launch {
            savedSearchRepository.insert(savedSearch)
        }
    }
}

data class SavedSearchPost(
    val savedSearch: SavedSearchServer,
    val posts: Flow<PagingData<Post>?>,
)

sealed class UiAction {
    data class GetSavedSearch(val clearAll: Boolean = false) : UiAction()
}