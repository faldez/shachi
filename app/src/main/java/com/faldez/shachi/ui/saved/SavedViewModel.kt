package com.faldez.shachi.ui.saved

import android.util.Log
import android.util.SparseArray
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.SavedSearch
import com.faldez.shachi.data.model.SavedSearchServer
import com.faldez.shachi.data.repository.FavoriteRepository
import com.faldez.shachi.data.repository.PostRepository
import com.faldez.shachi.data.repository.SavedSearchRepository
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

    init {
        savedSearchFlow = savedSearchRepository.getAllFlow().distinctUntilChanged()
            .map { data ->
                Log.d("SavedViewModel", "collect savedSearches")
                data.map { savedSearch ->
                    var posts = postsMap[savedSearch.savedSearch.savedSearchId]
                    if (posts == null) {
                        posts = getSearchPosts(savedSearch).cachedIn(viewModelScope)
                        postsMap[savedSearch.savedSearch.savedSearchId] = posts
                    }
                    SavedSearchPost(savedSearch = savedSearch, posts = posts)
                }
            }.shareIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                replay = 1
            )
    }

    private fun getSearchPosts(
        savedSearch: SavedSearchServer,
    ): Flow<PagingData<Post>> {
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
            savedSearchRepository.insert(savedSearch.tags,
                savedSearch.savedSearchTitle,
                savedSearch.serverId)
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

private const val LAST_SCROLL_POSITIONS: String = "last_scroll_positions"
