package com.faldez.shachi.ui.post_slide

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.faldez.shachi.R
import com.faldez.shachi.model.Post
import com.faldez.shachi.ui.favorite.FavoriteViewModel
import kotlinx.coroutines.flow.collect

class FavoritePostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "FavoritePostSlideFragment"
    }

    private val viewModel: FavoriteViewModel by navGraphViewModels(R.id.nav_graph)

    override suspend fun collectPagingData() {
        viewModel.pagingDataFlow.collect {
            postSlideAdapter.submitData(it)
        }
    }

    override fun navigateToPostSlide(post: Post?) {
        val bundle = bundleOf("post" to post,
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