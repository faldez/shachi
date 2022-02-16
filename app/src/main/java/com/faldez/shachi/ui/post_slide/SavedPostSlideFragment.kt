package com.faldez.shachi.ui.post_slide

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.faldez.shachi.R
import com.faldez.shachi.model.Post
import com.faldez.shachi.ui.saved.SavedViewModel

class SavedPostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "SavedPostSlideFragment"
    }

    private val viewModel: SavedViewModel by navGraphViewModels(R.id.saved)

    override suspend fun collectPagingData() {
        val savedSearchId = requireArguments().getInt("saved_search_id")
        viewModel.posts(savedSearchId).collect {
            if (it != null) {
                postSlideAdapter.submitData(it)
            }
        }
    }

    override fun navigateToPostSlide(post: Post?) {
        val server = requireArguments().get("server")
        val tags = requireArguments().get("tags")
        val bundle = bundleOf("post" to post,
            "server" to server,
            "tags" to tags)
        findNavController().navigate(R.id.action_postslide_to_postdetail, bundle)
    }

    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}