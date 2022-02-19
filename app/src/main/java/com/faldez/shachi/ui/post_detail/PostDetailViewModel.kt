package com.faldez.shachi.ui.post_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.TagDetail
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostDetailViewModel(
    val post: Post,
    private val serverRepository: ServerRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val state: StateFlow<UiState>
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        state =
            actionStateFlow.filterIsInstance<UiAction.SetSelectedTags>().distinctUntilChanged()
                .flatMapLatest { action ->
                    val server = serverRepository.getServerById(post.serverId)
                    val splittedTags = action.tags.split(" ").filter {
                        it.isNotEmpty() && it != "~"
                    }.groupBy { it.first() == '-' }

                    val includedTags = splittedTags[false]?.let {
                        tagRepository.getTags(Action.GetTags(server?.toServer(),
                            it.joinToString(" ")))
                    } ?: listOf()

                    Log.d("PostDetailViewModel/state", "includedTags=$includedTags")

                    val blacklistedTags = splittedTags[true]?.map { it.removePrefix("-") }?.let {
                        tagRepository.getTags(Action.GetTags(server?.toServer(),
                            it.joinToString(" ")))
                    } ?: listOf()

                    Log.d("PostDetailViewModel/state", "blacklistedTags=$blacklistedTags")

                    val postTags = tagRepository.getTags(Action.GetTags(server?.toServer(),
                        post.tags))?.filter {
                        !includedTags.contains(it) && !blacklistedTags.contains(it)
                    }?.map { TagDetail(name = it.name, type = it.type) }

                    Log.d("PostDetailViewModel/state", "postTags=$postTags")

                    flow {
                        emit(UiState(
                            tags = postTags,
                            includedTags = includedTags.map {
                                TagDetail(name = it.name,
                                    type = it.type)
                            },
                            blacklistedTags = blacklistedTags.map {
                                TagDetail(name = it.name,
                                    type = it.type,
                                    excluded = true)
                            }
                        ))
                    }
                }.stateIn(
                    scope = CoroutineScope(Dispatchers.IO),
                    started = SharingStarted.Eagerly,
                    initialValue = UiState()
                )

        accept =
            { action ->
                viewModelScope.launch {
                    actionStateFlow.emit(action)
                }
            }
    }
}

sealed class UiAction {
    data class SetPost(val post: Post) : UiAction()
    data class SetSelectedTags(val tags: String) : UiAction()
}

data class UiState(
    val tags: List<TagDetail>? = null,
    val includedTags: List<TagDetail>? = null,
    val blacklistedTags: List<TagDetail>? = null,
)