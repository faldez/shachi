package com.faldez.shachi.ui.browse

import androidx.navigation.navGraphViewModels
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.faldez.shachi.data.repository.post.PostRepositoryImpl
import com.faldez.shachi.data.repository.saved_search.SavedSearchRepositoryImpl
import com.faldez.shachi.data.repository.search_history.SearchHistoryRepositoryImpl
import com.faldez.shachi.data.repository.server.ServerRepositoryImpl
import com.faldez.shachi.service.BooruServiceImpl
import com.faldez.shachi.ui.browse.base.BaseBrowseFragment

class SavedBrowseFragment : BaseBrowseFragment() {
    override val viewModel: BrowseViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        val favoriteRepository = FavoriteRepositoryImpl(db)
        BrowseViewModelFactory(
            PostRepositoryImpl(BooruServiceImpl()),
            ServerRepositoryImpl(db),
            favoriteRepository,
            SavedSearchRepositoryImpl(db),
            SearchHistoryRepositoryImpl(db),
            this)
    }
}