package com.faldez.shachi.ui.search_simple

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.data.TagRepository
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.Tag
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchSimpleViewModel(
    val server: ServerView?,
    initialTags: List<Tag>,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val tags: StateFlow<List<Tag>?>
    val selectedTags: MutableStateFlow<List<Tag>> = MutableStateFlow(listOf())
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        tags =
            actionStateFlow.filterIsInstance<UiAction.SearchTag>()
                .distinctUntilChanged()
                .flatMapLatest { searchTag(server?.toServer(), it.tag) }.stateIn(scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(),
                    initialValue = null
                )

        accept = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
            }
        }

        viewModelScope.launch {
            selectedTags.getAndUpdate { _ ->
                if (initialTags.isNotEmpty()) {
                    tagRepository.getTags(Action.GetTags(server?.toServer(),
                        initialTags.joinToString(" ") { it.name })) ?: listOf()
                } else {
                    listOf()
                }
            }
        }
    }

    private fun searchTag(server: Server?, tag: String): Flow<List<Tag>?> = flow {
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

    fun insertTag(tag: Tag) {
        selectedTags.getAndUpdate { tags ->
            val list = tags.toMutableList()
            list.add(list.size, tag)
            list
        }
    }

    fun insertTagByName(name: String) {
        viewModelScope.launch {
            tagRepository.getTag(Action.GetTag(server?.toServer(), name)).let { tag ->
                selectedTags.getAndUpdate { tags ->
                    val list = tags.toMutableList()
                    list.add(list.size, tag ?: Tag.fromName(name))
                    list
                }
            }
        }
    }

    fun removeTag(tag: Tag) {
        selectedTags.getAndUpdate { tags ->
            val list = tags.toMutableList()
            list.remove(tag)
            list
        }
    }

    fun getTagsDetails(tags: List<Tag>) = viewModelScope.launch {
        selectedTags.getAndUpdate { tags ->
            tags.map {
                tagRepository.getTag(Action.GetTag(server?.toServer(), it.name)) ?: it
            }
        }
    }
}

sealed class UiAction {
    data class SearchTag(val server: Server?, val tag: String) : UiAction()
    data class InsertTag(val tag: String) : UiAction()
    object GetSelectedServer : UiAction()
}

data class UiState(
    val server: Server? = null,
)