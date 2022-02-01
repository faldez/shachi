package com.faldez.shachi.ui.server_edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.data.PostRepository
import com.faldez.shachi.data.ServerRepository
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerType
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ServerEditViewModel(
    val initialServer: ServerView?,
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
) : ViewModel() {
    private val isNew = initialServer == null
    val server: MutableStateFlow<ServerView?> = MutableStateFlow(initialServer)
    val state: MutableStateFlow<State> = MutableStateFlow(State.Idle)

    fun setTitle(title: String) {
        val value = server.value
        server.value = value?.copy(title = title)
            ?: ServerView(serverId = 0, title = title, type = ServerType.Gelbooru, url = "")
    }

    fun setUrl(url: String) {
        val value = server.value
        server.value = value?.copy(url = url) ?: ServerView(serverId = 0,
            title = "",
            type = ServerType.Gelbooru,
            url = url)
    }

    fun setType(type: ServerType) {
        val value = server.value
        server.value =
            value?.copy(type = type) ?: ServerView(serverId = 0, title = "", type = type, url = "")
    }

    fun validate(): Error? {
        val value = server.value ?: return Error("server is null")

        if (value.title.isEmpty()) {
            return Error("title can not be empty")
        }

        if (value.url.isEmpty()) {
            return Error("title can not be empty")
        }

        return null
    }

    fun test(server: ServerView) {
        viewModelScope.launch {
            Log.d("ServerEditViewModel", "Insert")
            try {
                postRepository.testSearchPost(Action.SearchPost(server, ""))
                if (isNew) {
                    serverRepository.insert(server.toServer())
                } else {
                    serverRepository.update(server.toServer())
                }
                state.value = State.Success
            } catch (e: Error) {
                state.value = State.Failed
            }
        }


    }
}

sealed class UiAction {
    data class EditTitle(val title: String) : UiAction()
    data class EditUrl(val url: String) : UiAction()
    data class EditType(val url: String) : UiAction()
    data class Insert(val server: Server) : UiAction()
    data class Test(val server: Server) : UiAction()
}

sealed class State {
    object Idle : State()
    object Success : State()
    object Failed : State()
}