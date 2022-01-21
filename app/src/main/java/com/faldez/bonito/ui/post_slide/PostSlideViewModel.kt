package com.faldez.bonito.ui.post_slide

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.faldez.bonito.data.GelbooruRepository
import com.faldez.bonito.model.Post
import kotlinx.coroutines.flow.Flow

class PostSlideViewModel constructor(
    private val repository: GelbooruRepository,
    initialData: List<Post>
) : ViewModel() {

}