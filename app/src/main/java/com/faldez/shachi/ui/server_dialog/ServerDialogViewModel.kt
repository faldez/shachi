package com.faldez.shachi.ui.server_dialog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.repository.ServerRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServerDialogViewModel(private val repository: ServerRepository) : ViewModel() {
    val serverList: Flow<List<ServerView>?>

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        serverList = actionStateFlow.filterIsInstance<UiAction.LoadAll>()
            .onStart { emit(UiAction.LoadAll) }.flatMapLatest {
                repository.getAllServersFlow()
            }
    }

    fun insert(serverId: Int) =
        viewModelScope.launch {
            Log.d("ServersViewModel", "$serverId")
            repository.setSelectedServer(serverId)
        }
}


sealed class UiAction {
    object LoadAll : UiAction()
}