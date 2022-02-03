package com.faldez.shachi.ui.post_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.data.ServerRepository
import com.faldez.shachi.data.TagRepository
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.Tag
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostDetailBottomSheetViewModel(
    val server: ServerView?,
    val post: Post,
    private val tagRepository: TagRepository,
    private val serverRepository: ServerRepository,
) : ViewModel() {
    val state: StateFlow<UiState>

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val getServerFlow =
            actionStateFlow.filterIsInstance<UiAction.GetServer>().distinctUntilChanged()
                .filter { server == null }
                .onStart { emit(UiAction.GetServer) }
                .flatMapLatest {
                    Log.d("PostDetailBottomSheetViewModel", "GetServer")
                    serverRepository.getServer(post.serverId)
                }

        val getTagsFlow =
            actionStateFlow.filterIsInstance<UiAction.GetTags>().distinctUntilChanged()
                .onStart { emit(UiAction.GetTags(server)) }
                .flatMapLatest {
                    flow {
                        Log.d("PostDetailBottomSheetViewModel", "GetTags")
                        emit(tagRepository.getTags(Action.GetTags(it.server?.toServer(),
                            post.tags)))
                    }
                }

        state = combine(getServerFlow, getTagsFlow, ::Pair).map { (server, tags) ->
            Log.d("PostDetailBottomSheetViewModel", "combine $server")
            UiState(
                post = post,
                tags = tags,
                server = server,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState(post, null, server)
        )

        viewModelScope.launch {
            state.collect {
                Log.d("PostDetailBottomSheetViewModel", "state ${it.server}")
                actionStateFlow.emit(UiAction.GetTags(it.server))
            }
        }
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