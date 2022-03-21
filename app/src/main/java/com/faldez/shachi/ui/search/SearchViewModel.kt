package com.faldez.shachi.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.faldez.shachi.data.api.Action
import com.faldez.shachi.data.model.*
import com.faldez.shachi.data.repository.search_history.SearchHistoryRepository
import com.faldez.shachi.data.repository.tag.TagRepository
import com.faldez.shachi.data.util.isManualSearchTags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchSimpleViewModel(
    private val server: ServerView?,
    private val tagRepository: TagRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {
    private val _state: MutableStateFlow<UiState> =
        MutableStateFlow(UiState(server = server, selectedTags = SelectedTags.Simple(
            listOf())))
    val state: StateFlow<UiState> = _state.asStateFlow()

    val searchHistoriesFlow: Flow<PagingData<SearchHistoryServer>>
    val suggestionTags: Flow<List<TagDetail>?>
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        searchHistoriesFlow = searchHistoryRepository.getAllFlow()
            .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

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

    private fun setInitialTagsSimple(value: String) =
        CoroutineScope(Dispatchers.IO).launch {
            val tags = value.split(" ")
                .mapNotNull { if (it.isNotEmpty()) TagDetail.fromName(it) else null }

            _state.update { currentState ->
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
                currentState.copy(selectedTags = SelectedTags.Simple(list))
            }
        }

    private fun setInitialTagsManual(tags: String) {
        _state.value =
            state.value.copy(selectedTags = SelectedTags.Manual(tags))
    }

    fun setInitialTags(tags: String) {
        if (tags.isManualSearchTags()) {
            setInitialTagsManual(tags)
        } else {
            setInitialTagsSimple(tags)
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
        } else if (selectedTags is SelectedTags.Manual) {
            _state.update {
                it.copy(selectedTags = SelectedTags.Manual(name))
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

    fun setMode(isManualMode: Boolean) {
        val tags = state.value.selectedTags
        if (isManualMode && tags is SelectedTags.Simple) {
            val tags = tags.tags.joinToString(" ")
            Log.d("SearchViewModel", "tags=$tags")
            setInitialTagsManual(tags)
        } else if (tags is SelectedTags.Manual) {
            if (tags.tags.isManualSearchTags()) {
                setInitialTagsSimple("")
            } else {
                setInitialTagsSimple(tags.tags)
            }
        }
    }

    fun setPage(page: Int?) {
        _state.value = _state.value.copy(page = page)
    }

    fun deleteSearchHistory(searchHistory: SearchHistory) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            searchHistoryRepository.delete(searchHistory)
        }
    }
}

sealed class SelectedTags {
    abstract fun isNotEmpty(): Boolean
    abstract fun asString(): String
    data class Simple(val tags: List<TagDetail>) : SelectedTags() {
        override fun isNotEmpty(): Boolean = tags.isNotEmpty()
        override fun asString(): String = tags.joinToString(" ")
    }

    data class Manual(val tags: String) : SelectedTags() {
        override fun isNotEmpty(): Boolean = tags.isNotEmpty()
        override fun asString(): String = tags
    }
}

sealed class UiAction {
    data class SearchTag(val tag: String) : UiAction()
}

data class UiState(
    val server: ServerView?,
    val selectedTags: SelectedTags,
    val page: Int? = null,
)