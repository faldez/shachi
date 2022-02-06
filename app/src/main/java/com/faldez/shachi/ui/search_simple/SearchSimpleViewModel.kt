package com.faldez.shachi.ui.search_simple

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.TagDetail
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchSimpleViewModel(
    private val tagRepository: TagRepository,
) : ViewModel() {
    val server: MutableStateFlow<ServerView?> = MutableStateFlow(null)
    val suggestionTags: StateFlow<List<TagDetail>?>
    val selectedTags: MutableStateFlow<List<TagDetail>> = MutableStateFlow(listOf())
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        suggestionTags =
            actionStateFlow.filterIsInstance<UiAction.SearchTag>()
                .distinctUntilChanged()
                .flatMapLatest { searchTag(it.server?.toServer(), it.tag) }
                .stateIn(scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(),
                    initialValue = null
                )

        accept = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
            }
        }
    }

    fun setServer(s: ServerView?) {
        server.value = s
    }

    fun setInitialTags(tags: List<TagDetail>) =
        CoroutineScope(Dispatchers.IO).launch {
            selectedTags.getAndUpdate { _ ->
                if (tags.isNotEmpty()) {
                    tagRepository.getTags(Action.GetTags(server.value?.toServer(),
                        tags.joinToString(" ") { it.name }))?.let { result ->
                        val resultMap = result.associateBy { tag -> tag.name }

                        tags.map {
                            it.copy(type = resultMap[it.name]?.type ?: 0)
                        }
                    } ?: listOf()
                } else {
                    listOf()
                }
            }
        }

    private fun searchTag(server: Server?, tag: String): Flow<List<TagDetail>?> = flow {
        val res = tagRepository.queryTags(Action.SearchTag(server, tag))
        Log.d("SearchSimpleViewModel", "res $res")
        emit(res)
    }

    fun toggleTag(name: String) {
        selectedTags.getAndUpdate { tags ->
            tags.map { tag ->
                if (tag.name == name) {
                    tag.copy(excluded = !tag.excluded)
                } else {
                    tag
                }
            }
        }
    }

    fun insertTag(tagDetail: TagDetail) {
        selectedTags.getAndUpdate { tags ->
            val list = tags.toMutableList()
            list.add(list.size, tagDetail)
            list
        }
    }

    fun insertTagByName(name: String) = CoroutineScope(Dispatchers.IO).launch {
        tagRepository.getTag(Action.GetTag(server.value?.toServer(), name)).let { tag ->
            selectedTags.getAndUpdate { tags ->
                val list = tags.toMutableList()
                list.add(list.size, TagDetail(name = name, type = tag?.type ?: 0))
                list
            }
        }
    }

    fun removeTag(tagDetail: TagDetail) {
        selectedTags.getAndUpdate { tags ->
            val list = tags.toMutableList()
            list.remove(tagDetail)
            list
        }
    }
}

sealed class UiAction {
    data class SearchTag(val server: ServerView?, val tag: String) : UiAction()
    data class InsertTag(val tag: String) : UiAction()
    object GetSelectedServer : UiAction()
}

data class UiState(
    val server: Server? = null,
)