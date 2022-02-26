package com.faldez.shachi.ui.share_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.repository.ServerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ShareDialogViewModel(serverRepository: ServerRepository, serverId: Int) : ViewModel() {
    val server: StateFlow<ServerView?> = serverRepository.getServer(serverId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )
}