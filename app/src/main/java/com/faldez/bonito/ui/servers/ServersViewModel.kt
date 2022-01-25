package com.faldez.bonito.ui.servers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.model.SelectedServer
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerWithSelected
import com.faldez.bonito.ui.search_post.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServersViewModel(private val repository: ServerRepository) : ViewModel() {
    val serverList: Flow<List<ServerWithSelected>?>
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        serverList = actionStateFlow.filterIsInstance<UiAction.LoadAll>()
            .onStart { emit(UiAction.LoadAll) }.flatMapLatest { repository.getAllServers() }


        accept = { action ->
            viewModelScope.launch {
                actionStateFlow.emit(action)
            }
        }
    }

    fun insert(serverUrl: String) {
        Log.d("ServersViewModel", "$serverUrl")
        viewModelScope.launch {
            repository.setSelectedServer(serverUrl)
        }
    }
}


sealed class UiAction {
    object LoadAll : UiAction()
    object LoadSelected : UiAction()
}