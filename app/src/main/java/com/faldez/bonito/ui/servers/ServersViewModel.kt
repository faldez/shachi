package com.faldez.bonito.ui.servers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerWithSelected
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServersViewModel(private val repository: ServerRepository) : ViewModel() {
    val serverList: Flow<List<ServerWithSelected>?>

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

    fun delete(server: ServerWithSelected) = viewModelScope.launch {
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
    data class Delete(val server: ServerWithSelected) : UiAction()
}