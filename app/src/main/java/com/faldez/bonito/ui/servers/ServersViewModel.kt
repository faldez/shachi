package com.faldez.bonito.ui.servers

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.model.Server
import com.faldez.bonito.ui.search_post.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ServersViewModel(private val db: AppDatabase) : ViewModel() {
    var serverList: Flow<List<Server>?>

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val load = actionStateFlow.filterIsInstance<UiAction.LoadAll>().distinctUntilChanged()
            .onStart { emit(UiAction.LoadAll) }
        serverList = load.flatMapLatest { getAllServer() }
    }

    private fun getAllServer(): Flow<List<Server>?> {
        return db.serverDao().getAll()
    }
}


sealed class UiAction {
    object LoadAll : UiAction()
}