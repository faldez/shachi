package com.faldez.shachi.ui.comment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.Comment
import com.faldez.shachi.repository.CommentRepository
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CommentViewModel(
    private val serverRepository: ServerRepository,
    private val commentRepository: CommentRepository,
) : ViewModel() {
    val commentFlow: Flow<List<Comment>?>
    val accept: (UiAction) -> Unit

    init {
        val actionSharedFlow = MutableSharedFlow<UiAction>()

        commentFlow =
            actionSharedFlow.filterIsInstance<UiAction.GetComment>().distinctUntilChanged()
                .map { action ->
                    serverRepository.getServerById(action.serverId)?.let { server ->
                        commentRepository.getComments(Action.GetComments(server = server.toServer(),
                            postId = action.postId))
                    }
                }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

        accept = { action ->
            viewModelScope.launch {
                actionSharedFlow.emit(action)
            }
        }
    }
}

sealed class UiAction {
    data class GetComment(val serverId: Int, val postId: Int) : UiAction()
}