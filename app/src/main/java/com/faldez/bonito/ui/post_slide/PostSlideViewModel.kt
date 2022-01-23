package com.faldez.bonito.ui.post_slide

import androidx.lifecycle.ViewModel
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.model.Post

class PostSlideViewModel constructor(
    private val postRepository: PostRepository,
    initialData: List<Post>
) : ViewModel() {

}