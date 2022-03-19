package com.faldez.shachi.ui.post_slide

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.navGraphViewModels
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.faldez.shachi.data.repository.post.PostRepositoryImpl
import com.faldez.shachi.data.repository.saved_search.SavedSearchRepositoryImpl
import com.faldez.shachi.service.BooruServiceImpl
import com.faldez.shachi.ui.post_slide.base.BasePostSlideFragment
import com.faldez.shachi.ui.saved.SavedViewModel
import com.faldez.shachi.ui.saved.SavedViewModelFactory

class SavedPostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "SavedPostSlideFragment"
    }

    private val viewModel: SavedViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        val service = BooruServiceImpl()
        SavedViewModelFactory(SavedSearchRepositoryImpl(db),
            PostRepositoryImpl(service), FavoriteRepositoryImpl(db), this)
    }
    private val savedSearchId by lazy { requireArguments().getInt("saved_search_id") }

    override suspend fun collectPagingData(showQuestionable: Boolean, showExplicit: Boolean) {
        viewModel.posts(savedSearchId).collect {
            if (it != null) {
                postSlideAdapter.submitData(it)
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