package com.faldez.shachi.ui.post_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.shachi.data.TagRepository
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.Tag
import com.faldez.shachi.service.Action
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailBottomSheetViewModel(
    private val server: ServerView?,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val state: MutableStateFlow<List<Tag>?> = MutableStateFlow(null)

    fun getTags(tags: String) {
        viewModelScope.launch {
            val tags = tagRepository.getTags(Action.GetTags(server?.toServer(), tags))
            state.update {
                tags?.sortedBy { t -> t.type }
            }
        }
    }
}