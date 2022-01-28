package com.faldez.bonito.ui.search_simple

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.data.TagRepository
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerWithSelected
import com.faldez.bonito.model.Tag
import com.faldez.bonito.service.Action
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchSimpleViewModel(
    val server: Server?,
    initialTags: List<Tag>,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val tags: StateFlow<List<Tag>?>
    var selectedTags: MutableStateFlow<List<Tag>> = MutableStateFlow(initialTags)
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        tags =
            actionStateFlow.filterIsInstance<UiAction.SearchTag>()
                .distinctUntilChanged()
                .flatMapLatest { searchTag(server, it.tag) }.stateIn(scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(),
                    initialValue = null
                )

        accept = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
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
            tagRepository.getTag(Action.GetTag(server, name))?.let { tag ->
                selectedTags.getAndUpdate { tags ->
                    val list = tags.toMutableList()
                    list.add(list.size, tag)
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
}

sealed class UiAction {
    data class SearchTag(val server: Server?, val tag: String) : UiAction()
    data class InsertTag(val tag: String) : UiAction()
    object GetSelectedServer : UiAction()
}

data class UiState(
    val server: Server? = null,
)