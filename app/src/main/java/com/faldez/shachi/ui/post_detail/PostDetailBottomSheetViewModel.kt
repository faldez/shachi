package com.faldez.shachi.ui.post_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.Tag
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostDetailBottomSheetViewModel(
    server: ServerView?,
    post: Post,
    private val tagRepository: TagRepository,
    private val serverRepository: ServerRepository,
) : ViewModel() {
    val state: StateFlow<UiState>

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        val getTagsFlow =
            actionStateFlow.filterIsInstance<UiAction.GetTags>().distinctUntilChanged()
                .onStart { emit(UiAction.GetTags(server)) }
                .filter { it.server != null }
                .flatMapLatest {
                    flow {
                        Log.d("PostDetailBottomSheetViewModel", "GetTags")
                        emit(tagRepository.getTags(Action.GetTags(it.server?.toServer(),
                            post.tags)))
                    }
                }.shareIn(
                    scope = CoroutineScope(Dispatchers.IO),
                    started = SharingStarted.WhileSubscribed(),
                    replay = 1
                )

        state = if (server == null) {
            val getServerFlow =
                actionStateFlow.filterIsInstance<UiAction.GetServer>()
                    .distinctUntilChanged()
                    .onStart { emit(UiAction.GetServer) }
                    .flatMapLatest {
                        Log.d("PostDetailBottomSheetViewModel", "GetServer $server")
                        serverRepository.getServer(post.serverId)
                    }
            combine(getServerFlow, getTagsFlow, ::Pair).map { (server, tags) ->
                Log.d("PostDetailBottomSheetViewModel", "combine $server")
                UiState(
                    post = post,
                    tags = tags,
                    server = server,
                )
            }
        } else {
            getTagsFlow.map { tags ->
                Log.d("PostDetailBottomSheetViewModel", "combine $server")
                UiState(
                    post = post,
                    tags = tags,
                    server = server,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState(post, null, server)
        )
    }
}

sealed class UiAction {
    object GetServer : UiAction()
    data class GetTags(val server: ServerView?) : UiAction()
}

data class UiState(
    val post: Post,
    val tags: List<Tag>?,
    val server: ServerView?,
)