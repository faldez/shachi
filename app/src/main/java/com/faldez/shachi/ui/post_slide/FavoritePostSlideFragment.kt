package com.faldez.shachi.ui.post_slide

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.filter
import androidx.paging.map
import com.faldez.shachi.R
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.ui.favorite.FavoriteViewModel

class FavoritePostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "FavoritePostSlideFragment"
    }

    private val viewModel: FavoriteViewModel by navGraphViewModels(R.id.nav_graph)

    override suspend fun collectPagingData(showQuestionable: Boolean, showExplicit: Boolean) {
        viewModel.pagingDataFlow.collect {
            val data = it.filter { post ->
                when (post.rating) {
                    Rating.Questionable -> showQuestionable
                    Rating.Explicit -> showExplicit
                    Rating.Safe -> true
                }
            }.map { post ->
                post.favorite = true
                post
            }
            postSlideAdapter.submitData(data)
        }
    }

    override fun navigateToPostSlide(post: Post?) {
        val bundle = bundleOf("post" to post)
        findNavController().navigate(R.id.action_postslide_to_postdetail, bundle)
    }


    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}