package com.faldez.shachi.ui.post_slide

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.faldez.shachi.R
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.mapToTags
import com.faldez.shachi.ui.browse.BrowseViewModel

class SavedPostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "PostSlideFragment"
    }

    private val viewModel: BrowseViewModel by navGraphViewModels(R.id.saved)

    override suspend fun collectPagingData() {
        viewModel.pagingDataFlow.collect {
            postSlideAdapter.submitData(it)
        }
    }

    override fun navigateToPostSlide(post: Post?) {
        val bundle = bundleOf("post" to post,
            "server" to viewModel.state.value.server,
            "tags" to viewModel.state.value.tags)
        findNavController().navigate(R.id.action_postslide_to_postdetail, bundle)
    }

    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}