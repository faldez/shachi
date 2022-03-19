package com.faldez.shachi.ui.post_slide

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.navGraphViewModels
import androidx.paging.filter
import androidx.paging.map
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.Rating
import com.faldez.shachi.data.repository.favorite.FavoriteRepository
import com.faldez.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.faldez.shachi.ui.favorite.FavoriteViewModel
import com.faldez.shachi.ui.favorite.FavoriteViewModelFactory
import com.faldez.shachi.ui.post_slide.base.BasePostSlideFragment

class FavoritePostSlideFragment : BasePostSlideFragment() {
    companion object {
        const val TAG = "FavoritePostSlideFragment"
    }

    private val viewModel: FavoriteViewModel by navGraphViewModels(R.id.favorite) {
        val db = AppDatabase.build(requireContext())
        FavoriteViewModelFactory(FavoriteRepositoryImpl(db), this)
    }

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

    override fun navigateToPostDetailBundle(post: Post?): Bundle {
        return bundleOf("post" to post,
            "tags" to viewModel.state.value.tags)
    }


    override fun deleteFavoritePost(post: Post) {
        viewModel.deleteFavoritePost(post)
    }

    override fun favoritePost(post: Post) {
        viewModel.favoritePost(post)
    }
}