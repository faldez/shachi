package com.faldez.bonito.ui.servers

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.database.AppDatabase
import com.faldez.bonito.ui.search_post.SearchPostViewModel
import java.lang.IllegalArgumentException

class ServersViewModelFactory constructor(
    private val db: AppDatabase,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(ServersViewModel::class.java)) {
            ServersViewModel(this.db) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}