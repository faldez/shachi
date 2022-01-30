package com.faldez.sachi.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.faldez.sachi.data.PostRepository
import com.faldez.sachi.data.SavedSearchRepository

class SavedViewModelFactory(
    private val savedSearchRepository: SavedSearchRepository,
    private val postRepository: PostRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SavedViewModel::class.java)) {
            SavedViewModel(this.savedSearchRepository, this.postRepository) as T
        } else {
            throw  IllegalArgumentException("ViewModel Not Found")
        }
    }
}