package com.faldez.shachi.ui.post_slide

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.navGraphViewModels
import androidx.paging.filter
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.repository.FavoriteRepository
import com.faldez.shachi.repository.PostRepository
import com.faldez.shachi.repository.SavedSearchRepository
import com.faldez.shachi.service.BooruServiceImpl
import com.faldez.shachi.ui.saved.SavedViewModel
import com.faldez.shachi.ui.saved.SavedViewModelFactory

class SavedPostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "SavedPostSlideFragment"
    }

    private val viewModel: SavedViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        val service = BooruServiceImpl()
        SavedViewModelFactory(SavedSearchRepository(db),
            PostRepository(service), FavoriteRepository(db), this)
    }
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

    override fun navigateToPostDetailBundle(post: Post?): Bundle {
        val server = requireArguments().get("server")
        val tags = requireArguments().get("tags")
        return bundleOf("post" to post,
            "server" to server,
            "tags" to tags)
    }

    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}