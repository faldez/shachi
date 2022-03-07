package com.faldez.shachi.ui.more

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.data.model.*
import com.faldez.shachi.data.repository.*
import com.faldez.shachi.service.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MoreViewModel(
    private val serverRepository: ServerRepository,
    private val blacklistTagRepository: BlacklistTagRepository,
    private val savedSearchRepository: SavedSearchRepository,
    private val favoriteRepository: FavoriteRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val backupFlow: Flow<BackupData>
    val restoreState: MutableStateFlow<RestoreState> = MutableStateFlow(RestoreState.Idle)
    val accept: (UiAction) -> Unit

    init {
        val actionSharedFlow = MutableSharedFlow<UiAction>()

        backupFlow = actionSharedFlow.filterIsInstance<UiAction.Backup>().map {
            BackupData(data = backup(), uri = it.uri)
        }.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1)


        accept = { action ->
            viewModelScope.launch {
                actionSharedFlow.emit(action)
            }
        }
    }

    fun restore(data: Backup) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val serverIdMap: MutableMap<Int, Int> = mutableMapOf()
            data.servers?.forEach { backupServer ->
                val server = serverRepository.getServerByUrl(backupServer.url)
                val serverId = server?.serverId ?: serverRepository.insert(
                    backupServer.copy(serverId = 0)).toInt()

                serverIdMap[backupServer.serverId] = serverId

                if (server?.type == ServerType.Moebooru) {
                    val tagsSummary =
                        tagRepository.getTagsSummary(Action.GetTagsSummary(server = backupServer.copy(
                            serverId = serverId)))
                    if (tagsSummary != null) {
                        tagRepository.insertTags(tagsSummary)
                    }

                }
            }

            //restore blacklist tag
            data.blacklistedTags?.forEach {
                blacklistTagRepository.insertBlacklistedTag(it)
            }

            // restore blacklist crossref
            data.blacklistedTagsCrossRef?.let {
                val list =
                    it.mapNotNull { ref ->
                        serverIdMap[ref.serverId]?.let { serverId ->
                            ref.copy(serverId = serverId)
                        }
                    }
                blacklistTagRepository.insertBlacklistedTag(list)
            }

            // restore saved search
            data.savedSearches?.forEach {
                serverIdMap[it.serverId]?.let { serverId ->
                    savedSearchRepository.insert(tags = it.tags,
                        title = it.savedSearchTitle,
                        serverId = serverId)
                }
            }

            // restore favorites
            data.favorites?.forEach {
                serverIdMap[it.serverId]?.let { serverId ->
                    favoriteRepository.insert(it.copy(serverId = serverId))
                }
            }

            // restore search histories
            data.searchHistories?.forEach {
                serverIdMap[it.serverId]?.let { serverId ->
                    searchHistoryRepository.insert(it.copy(serverId = serverId))
                }
            }
            restoreState.value = RestoreState.Success
        } catch (e: Error) {
            Log.d("ServerEditViewModel/test", "$e")
            restoreState.value = RestoreState.Failed("$e")
        }
    }

    private suspend fun backup(): Backup {
        val blacklistedTags = getBlacklistedTags()
        val blacklistedTagsCrossRef = blacklistedTags?.flatMap { blacklistedTag ->
            blacklistedTag.servers.map { server ->
                ServerBlacklistedTagCrossRef(serverId = server.serverId,
                    blacklistedTagId = blacklistedTag.blacklistedTag.blacklistedTagId)
            }
        }

        return Backup(
            servers = getServers(),
            blacklistedTags = blacklistedTags?.map { it.blacklistedTag },
            blacklistedTagsCrossRef = blacklistedTagsCrossRef,
            savedSearches = getSavedSearches(),
            favorites = getFavorites(),
            searchHistories = getSearchHistories(),
        )
    }


    private suspend fun getServers(): List<Server>? {
        return serverRepository.getAllServers()
    }

    private suspend fun getBlacklistedTags(): List<BlacklistedTagWithServer>? {
        return blacklistTagRepository.getAll()
    }

    private suspend fun getSavedSearches(): List<SavedSearch>? {
        return savedSearchRepository.getAll()
    }

    private suspend fun getFavorites(): List<Post>? {
        return favoriteRepository.getAll()
    }

    private suspend fun getSearchHistories(): List<SearchHistory>? {
        return searchHistoryRepository.getAll()
    }
}

sealed class UiAction {
    data class Backup(val uri: Uri) : UiAction()
}

sealed class RestoreState {
    object Idle : RestoreState()
    object Start : RestoreState()
    data class Loaded(val data: Backup) : RestoreState()
    object Success : RestoreState()
    data class Failed(val reason: String) : RestoreState()
}

data class BackupData(
    val data: Backup,
    val uri: Uri,
)