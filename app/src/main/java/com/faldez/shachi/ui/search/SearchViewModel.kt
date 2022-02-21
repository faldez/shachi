package com.faldez.shachi.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.*
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
    private val _state: MutableStateFlow<UiState> = MutableStateFlow(UiState(server = server))
    val state: StateFlow<UiState> = _state.asStateFlow()

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
            _state.update { currentState ->
                Log.d("SearchSimpleViewModel/setInitialTagsSimple", "$tags")
                val list = if (tags.isNotEmpty()) {
                    val tagsString = tags.joinToString(" ") { it.name }
                    tagRepository.getTags(Action.GetTags(server?.toServer(), tagsString))
                        ?.let { result ->
                            val resultMap = result.associateBy { tag -> tag.name }
                            tags.map {
                                it.copy(type = resultMap[it.name]?.type ?: Category.Unknown)
                            }
                        } ?: listOf()
                } else {
                    listOf()
                }
                currentState.copy(selectedTags = SelectedTags.Simple(list), isAdvancedMode = false)
            }
        }

    private fun setInitialTagsAdvance(tags: String) {
        _state.value =
            state.value.copy(selectedTags = SelectedTags.Advance(tags), isAdvancedMode = true)
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
        emit(res)
    }

    fun toggleTag(name: String) {
        val selectedTags = state.value.selectedTags
        if (selectedTags is SelectedTags.Simple) {
            val list = selectedTags.tags.toMutableList()
            list.replaceAll {
                if (it.name == name) {
                    it.copy(modifier = if (it.modifier == Modifier.Minus) null else Modifier.Minus)
                } else {
                    it
                }
            }
            Log.d("SearchSimpleViewModel/toggleTag", "$list")
            _state.update {
                it.copy(selectedTags = SelectedTags.Simple(list))
            }
        }
    }

    fun insertTag(tagDetail: TagDetail) {
        val selectedTags = state.value.selectedTags
        if (selectedTags is SelectedTags.Simple) {
            val list = selectedTags.tags.toMutableList()
            list.add(list.size, tagDetail)
            _state.update {
                it.copy(selectedTags = SelectedTags.Simple(list))
            }
        }
    }

    fun insertTagByName(name: String) = CoroutineScope(Dispatchers.IO).launch {
        val selectedTags = state.value.selectedTags
        if (selectedTags is SelectedTags.Simple) {
            val list = selectedTags.tags.toMutableList()
            val tag = tagRepository.getTag(Action.GetTag(server?.toServer(), name))
            list.add(list.size, TagDetail(name = name, type = tag?.type ?: Category.Unknown))
            _state.update {
                it.copy(selectedTags = SelectedTags.Simple(list))
            }
        } else if (selectedTags is SelectedTags.Advance) {
            _state.update {
                it.copy(selectedTags = SelectedTags.Advance(name))
            }
        }
    }

    fun removeTag(tagDetail: TagDetail) {
        val selectedTags = state.value.selectedTags
        if (selectedTags is SelectedTags.Simple) {
            val list = selectedTags.tags.toMutableList()
            list.remove(tagDetail)
            _state.update {
                it.copy(selectedTags = SelectedTags.Simple(list))
            }
        }
    }

    fun setMode(mode: Boolean) {
        _state.value = state.value.copy(isAdvancedMode = mode)
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