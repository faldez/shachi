package com.faldez.bonito.ui.post_slide

import androidx.lifecycle.ViewModel
import com.faldez.bonito.data.Repository
import com.faldez.bonito.model.Post

class PostSlideViewModel constructor(
    private val repository: Repository,
    initialData: List<Post>
) : ViewModel() {

}