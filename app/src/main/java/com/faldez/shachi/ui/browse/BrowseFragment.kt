package com.faldez.shachi.ui.browse

import androidx.navigation.navGraphViewModels
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.repository.*
import com.faldez.shachi.service.BooruServiceImpl

class BrowseFragment : BaseBrowseFragment() {
    override val viewModel: BrowseViewModel by navGraphViewModels(R.id.browse) {
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