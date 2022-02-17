package com.faldez.shachi.ui.post_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.Tag
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class PostDetailBottomSheetViewModel(
    currentTags: String?,
    post: Post,
    private val serverRepository: ServerRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val state: StateFlow<UiState>

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val getPostTags = actionStateFlow
            .filterIsInstance<UiAction.GetTags>().distinctUntilChanged()
            .onStart { emit(UiAction.GetTags) }
            .flatMapLatest {
                flow {
                    Log.d("PostDetailBottomSheetViewModel", "GetTags")
                    val server = serverRepository.getServerById(post.serverId)
                    emit(tagRepository.getTags(Action.GetTags(server?.toServer(), post.tags)))
                }
            }
        state = if (currentTags == null) {
            getPostTags.map { tags ->
                val server = serverRepository.getServerById(post.serverId)
                Log.d("PostDetailBottomSheetViewModel", "combine $server")
                UiState(
                    post = post,
                    tags = tags,
                    server = server,
                    currentTags = listOf()
                )
            }
        } else {
            val getCurrentTags = actionStateFlow
                .filterIsInstance<UiAction.GetTags>().distinctUntilChanged()
                .onStart { emit(UiAction.GetTags) }.flatMapLatest {
                    flow {
                        val server = serverRepository.getServerById(post.serverId)
                        Log.d("PostDetailBottomSheetViewModel", "GetTags")
                        emit(tagRepository.getTags(Action.GetTags(server?.toServer(),
                            post.tags)))
                    }
                }
            getPostTags.zip(getCurrentTags) { postTags, tags ->
                val server = serverRepository.getServerById(post.serverId)
                UiState(
                    post = post,
                    tags = postTags,
                    server = server,
                    currentTags = tags
                )
            }
        }.stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState(post, null, null, server = null)
        )
    }
}

sealed class UiAction {
    object GetServer : UiAction()
    object GetTags : UiAction()
}

data class UiState(
    val post: Post,
    val tags: List<Tag>?,
    val currentTags: List<Tag>?,
    val server: ServerView?,
)