package com.faldez.bonito.ui.server_edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.model.Server
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ServerEditViewModel(private val repository: ServerRepository) : ViewModel() {
    fun insert(server: Server) {
        viewModelScope.launch {
            Log.d("ServerEditViewModel", "Insert")
            repository.insert(server)
        }
    }
}

sealed class UiAction {
    data class Insert(val server: Server) : UiAction()
}