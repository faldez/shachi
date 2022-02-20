package com.faldez.shachi.ui.post_detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.model.*
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.Action
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PostDetailViewModel(
    val post: Post,
    private val initialSearchTags: String,
    private val serverRepository: ServerRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val appendedTagsState: MutableStateFlow<List<TagDetailState>> =
        MutableStateFlow(listOf())
    val state: StateFlow<UiState>
    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()
        val postTagsFlow =
            actionStateFlow.filterIsInstance<UiAction.SetInitialTags>().distinctUntilChanged()
                .onStart { emit(UiAction.SetInitialTags(initialSearchTags)) }.map {
                    val server = serverRepository.getServerById(post.serverId)

                    val (included, blacklisted) = stringToTags(server = server?.toServer(),
                        tags = initialSearchTags)

                    val includedSet = included.toSet()

                    val postTags = tagRepository.getTags(Action.GetTags(server?.toServer(),
                        post.tags))?.map {
                        TagDetailState(
                            tag = TagDetail.fromTag(it),
                            checked = included.contains(it),
                            mutable = !includedSet.contains(it)
                        )
                    } ?: listOf()

                    val blacklistedTags = blacklisted.map {
                        TagDetailState(
                            tag = TagDetail.fromTag(it).copy(modifier = Modifier.Minus),
                            checked = true,
                            mutable = false
                        )
                    }

                    val tags = listOf(postTags, blacklistedTags).flatten()

                    Pair(server, tags)
                }


        state = combine(
            postTagsFlow,
            appendedTagsState.asSharedFlow(),
            ::Pair).map { (postTags, appendedTags) ->

            Log.d("PostDetailViewModel/state", "appendedTags=$appendedTags")

            val appendedTagsMap = appendedTags.associateBy { it.tag.name }
            val tags = postTags.second.map {
                appendedTagsMap[it.tag.name] ?: it
            }


            UiState(
                initialSearchTags = initialSearchTags,
                server = postTags.first,
                tags = tags,
            )
        }.stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = UiState(
                initialSearchTags = initialSearchTags
            )
        )

        accept =
            { action ->
                viewModelScope.launch {
                    actionStateFlow.emit(action)
                }
            }
    }

    private suspend fun stringToTags(server: Server?, tags: String): Pair<List<Tag>, List<Tag>> {
        val splittedTags =
            tags.split(" ").filter {
                it.isNotEmpty() && it != "~"
            }.groupBy { it.first() == '-' }

        Log.d("PostDetailViewModel/splittedTags", "$splittedTags")

        val includedTags = splittedTags[false]?.let {
            tagRepository.getTags(Action.GetTags(server,
                it.joinToString(" ")))
        } ?: listOf()

        Log.d("PostDetailViewModel/state", "includedTags=$includedTags")

        val blacklistedTags = splittedTags[true]?.map { it.removePrefix("-") }?.let {
            tagRepository.getTags(Action.GetTags(server,
                it.joinToString(" ")))
        } ?: listOf()

        Log.d("PostDetailViewModel/state", "blacklistedTags=$blacklistedTags")

        return Pair(includedTags, blacklistedTags)
    }

    fun addTag(tag: TagDetailState) {
        Log.d("PostDetailViewModel/setTag", "tag=$tag")
        appendedTagsState.update { list ->
            val mutableList = list.toMutableList()
            mutableList.add(tag)
            mutableList.toList()
        }
    }

    fun removeTag(tag: TagDetailState) {
        Log.d("PostDetailViewModel/removeTag", "tag=$tag")
        appendedTagsState.update { list ->
            val mutableList = list.toMutableList()
            mutableList.remove(tag)
            mutableList.toList()
        }
    }
}

sealed class UiAction {
    data class SetPost(val post: Post) : UiAction()
    data class SetInitialTags(val tags: String) : UiAction()
    data class SetTag(val tag: TagDetailState? = null) : UiAction()
    data class GetPostTags(val tags: String) : UiAction()
}

data class TagDetailState(
    val tag: TagDetail,
    val checked: Boolean = false,
    val mutable: Boolean = false,
)

data class UiState(
    val initialSearchTags: String,
    val server: ServerView? = null,
    val tags: List<TagDetailState>? = null,

    )