package com.faldez.bonito.ui.post_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faldez.bonito.data.TagRepository
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.Tag
import com.faldez.bonito.service.Action
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailBottomSheetViewModel(
    private val server: Server?,
    private val tagRepository: TagRepository,
) : ViewModel() {
    val state: MutableStateFlow<List<Tag>?> = MutableStateFlow(null)

    fun getTags(tags: String) {
        viewModelScope.launch {
            val tags = tagRepository.getTags(Action.GetTags(server, tags))
            state.update {
                tags?.sortedBy { t -> t.type }
            }
        }
    }
}