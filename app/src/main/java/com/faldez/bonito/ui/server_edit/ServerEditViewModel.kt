package com.faldez.bonito.ui.server_edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.model.Server
import com.faldez.bonito.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServerEditViewModel(
    private val postRepository: PostRepository,
    private val serverRepository: ServerRepository,
) : ViewModel() {
    val state: MutableStateFlow<State> = MutableStateFlow(State.Idle)

    fun insert(server: Server) {
        viewModelScope.launch {
            Log.d("ServerEditViewModel", "Insert")
            serverRepository.insert(server)
        }
    }

    fun insertFlow(server: Server) = flow {
        Log.d("ServerEditViewModel", "Insert")
        emit(serverRepository.insert(server))
    }

    fun test(server: Server) {
        viewModelScope.launch {
            Log.d("ServerEditViewModel", "Insert")
            try {
                postRepository.testSearchPost(Action.SearchPost(server, ""))
                serverRepository.insert(server)
                state.value = State.Success
            } catch(e: Error) {
                state.value = State.Failed
            }
        }


    }
}

sealed class UiAction {
    data class Insert(val server: Server) : UiAction()
    data class Test(val server: Server) : UiAction()
}

sealed class State {
    object Idle : State()
    object Success : State()
    object Failed : State()
}