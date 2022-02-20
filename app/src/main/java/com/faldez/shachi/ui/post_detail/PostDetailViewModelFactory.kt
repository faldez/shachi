package com.faldez.shachi.ui.post_detail

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.faldez.shachi.model.Post
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository

class PostDetailViewModelFactory constructor(
    private val post: Post,
    private val initialSearchTags: String,
    private val serverRepository: ServerRepository,
    private val tagRepository: TagRepository,
    owner: SavedStateRegistryOwner,
) : AbstractSavedStateViewModelFactory(owner, null) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        return if (modelClass.isAssignableFrom(PostDetailViewModel::class.java)) {
            PostDetailViewModel(
                post,
                initialSearchTags,
                serverRepository,
                tagRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }

}