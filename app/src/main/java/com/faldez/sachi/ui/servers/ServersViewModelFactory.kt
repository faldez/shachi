package com.faldez.sachi.ui.servers

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.sachi.data.ServerRepository
import java.lang.IllegalArgumentException

class ServersViewModelFactory constructor(
    private val repository: ServerRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(ServersViewModel::class.java)) {
            ServersViewModel(this.repository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}