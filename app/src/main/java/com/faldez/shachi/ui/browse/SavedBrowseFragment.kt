package com.faldez.shachi.ui.browse

import androidx.navigation.navGraphViewModels
import com.faldez.shachi.R
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.repository.*
import com.faldez.shachi.service.BooruServiceImpl
import com.faldez.shachi.ui.browse.base.BaseBrowseFragment

class SavedBrowseFragment : BaseBrowseFragment() {
    override val viewModel: BrowseViewModel by navGraphViewModels(R.id.saved) {
        val db = AppDatabase.build(requireContext())
        val favoriteRepository = FavoriteRepository(db)
        BrowseViewModelFactory(
            PostRepository(BooruServiceImpl()),
            ServerRepository(db),
            favoriteRepository,
            SavedSearchRepository(db),
            SearchHistoryRepository(db),
            this)
    }
}