package com.faldez.shachi.ui.post_slide

import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.filter
import com.faldez.shachi.R
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.ui.saved.SavedViewModel

class SavedPostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "SavedPostSlideFragment"
    }

    private val viewModel: SavedViewModel by navGraphViewModels(R.id.saved)
    private val savedSearchId by lazy { requireArguments().getInt("saved_search_id") }

    override suspend fun collectPagingData(showQuestionable: Boolean, showExplicit: Boolean) {
        viewModel.posts(savedSearchId).collect {
            if (it != null) {
                postSlideAdapter.submitData(it.filter { post ->
                    when (post.rating) {
                        Rating.Questionable -> showQuestionable
                        Rating.Explicit -> showExplicit
                        Rating.Safe -> true
                    }
                })
            }
        }
    }

    override fun onPageChange(position: Int) {
        viewModel.putScroll(savedSearchId, position)
    }

    override fun navigateToPostDetail(post: Post?) {
        val server = requireArguments().get("server")
        val tags = requireArguments().get("tags")
        val bundle = bundleOf("post" to post,
            "server" to server,
            "tags" to tags)
        findNavController().navigate(R.id.action_savedpostslide_to_postdetail, bundle)
    }

    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}