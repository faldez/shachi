package com.faldez.shachi.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.Category
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
    private val server: ServerView?,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val state: MutableStateFlow<UiState> = MutableStateFlow(UiState(server = server))
    val suggestionTags: Flow<List<TagDetail>?>
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        suggestionTags =
            actionStateFlow.filterIsInstance<UiAction.SearchTag>()
                .distinctUntilChanged()
                .flatMapLatest { searchTag(server?.toServer(), it.tag) }
                .shareIn(scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(),
                    replay = 1
                )

        accept = {
            viewModelScope.launch {
                actionStateFlow.emit(it)
            }
        }
    }

    private fun setInitialTagsSimple(tags: List<TagDetail>) =
        CoroutineScope(Dispatchers.IO).launch {
            state.getAndUpdate { currentState ->
                Log.d("SearchSimpleViewModel/setInitialTagsSimple", "$tags")
                val list = if (tags.isNotEmpty()) {
                    tagRepository.getTags(Action.GetTags(server?.toServer(),
                        tags.joinToString(" ") { it.name }))?.let { result ->
                        val resultMap = result.associateBy { tag -> tag.name }
                        tags.map {
                            it.copy(type = resultMap[it.name]?.type ?: Category.General)
                        }
                    } ?: listOf()
                } else {
                    listOf()
                }
                currentState.copy(selectedTags = SelectedTags.Simple(list), isAdvancedMode = false)
            }
        }

    private fun setInitialTagsAdvance(tags: String) {
        state.value = state.value.copy(selectedTags = SelectedTags.Advance(tags), isAdvancedMode = true)
    }

    fun setInitialTags(tags: String, isAdvancedMode: Boolean) {
        if (isAdvancedMode) {
            setInitialTagsAdvance(tags)
        } else {
            val value = tags.split(" ")
                .mapNotNull { if (it.isNotEmpty()) TagDetail.fromName(it) else null }
            setInitialTagsSimple(value)
        }
    }

    private fun searchTag(server: Server?, tag: String): Flow<List<TagDetail>?> = flow {
        val res = tagRepository.queryTags(Action.SearchTag(server, tag))
        Log.d("SearchSimpleViewModel", "res $res")
        emit(res)
    }

    fun toggleTag(name: String) {
        state.getAndUpdate { currentState ->
            val tags = currentState.selectedTags.let {
                if (it is SelectedTags.Simple) {
                    SelectedTags.Simple(it.tags.map { tag ->
                        if (tag.name == name) {
                            tag.copy(excluded = !tag.excluded)
                        } else {
                            tag
                        }
                    })
                } else {
                    it
                }
            }
            currentState.copy(selectedTags = tags)
        }
    }

    fun insertTag(tagDetail: TagDetail) {
        state.getAndUpdate { currentState ->
            val tags = currentState.selectedTags.let {
                if (it is SelectedTags.Simple) {
                    val list = it.tags.toMutableList()
                    list.add(list.size, tagDetail)
                    SelectedTags.Simple(list)
                } else {
                    it
                }
            }
            currentState.copy(selectedTags = tags)
        }
    }

    fun insertTagByName(name: String) = CoroutineScope(Dispatchers.IO).launch {
        tagRepository.getTag(Action.GetTag(server?.toServer(), name)).let { tag ->
            state.getAndUpdate { currentState ->
                val tags = currentState.selectedTags.let {
                    if (it is SelectedTags.Simple) {
                        val list = it.tags.toMutableList()
                        list.add(list.size, TagDetail(name = name, type = tag?.type ?: Category.General))
                        SelectedTags.Simple(list)
                    } else {
                        it
                    }
                }
                currentState.copy(selectedTags = tags)
            }
        }
    }

    fun removeTag(tagDetail: TagDetail) {
        state.getAndUpdate { currentState ->
            val tags = currentState.selectedTags.let {
                if (it is SelectedTags.Simple) {
                    val list = it.tags.toMutableList()
                    list.remove(tagDetail)
                    SelectedTags.Simple(list)
                } else {
                    it
                }
            }
            currentState.copy(selectedTags = tags)
        }
    }

    fun setMode(mode: Boolean) {
        state.value = state.value.copy(isAdvancedMode = mode)
    }
}

sealed class SelectedTags {
    abstract fun isNotEmpty(): Boolean
    data class Simple(val tags: List<TagDetail>) : SelectedTags() {
        override fun isNotEmpty(): Boolean = tags.isNotEmpty()
    }

    data class Advance(val tags: String) : SelectedTags() {
        override fun isNotEmpty(): Boolean = tags.isNotEmpty()
    }
}

sealed class UiAction {
    data class SearchTag(val tag: String) : UiAction()
}

data class UiState(
    val server: ServerView?,
    val isAdvancedMode: Boolean = false,
    val selectedTags: SelectedTags? = null,
)