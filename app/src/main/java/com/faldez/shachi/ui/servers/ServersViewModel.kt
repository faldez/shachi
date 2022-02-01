package com.faldez.shachi.ui.servers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.data.ServerRepository
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServersViewModel(private val repository: ServerRepository) : ViewModel() {
    val serverList: Flow<List<ServerView>?>

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        serverList = actionStateFlow.filterIsInstance<UiAction.LoadAll>()
            .onStart { emit(UiAction.LoadAll) }.flatMapLatest { repository.getAllServers() }
    }

    fun insert(serverId: Int) =
        viewModelScope.launch {
            Log.d("ServersViewModel", "$serverId")
            repository.setSelectedServer(serverId)
        }

    fun delete(server: ServerView) = viewModelScope.launch {
        repository.delete(server = Server(
            serverId = server.serverId,
            type = server.type,
            title = server.title,
            url = server.url
        ))
    }
}


sealed class UiAction {
    object LoadAll : UiAction()
    data class Delete(val server: ServerView) : UiAction()
}