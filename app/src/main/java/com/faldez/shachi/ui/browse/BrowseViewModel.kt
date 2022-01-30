package com.faldez.shachi.ui.browse

import androidx.lifecycle.SavedStateHandle
import com.faldez.shachi.data.FavoriteRepository
import com.faldez.shachi.data.PostRepository
import com.faldez.shachi.data.SavedSearchRepository
import com.faldez.shachi.data.ServerRepository
import com.faldez.shachi.model.Server
import com.faldez.shachi.ui.base.BaseBrowseViewModel

class BrowseViewModel constructor(
    server: Server?,
    tags: String?,
    postRepository: PostRepository,
    serverRepository: ServerRepository,
    favoriteRepository: FavoriteRepository,
    savedSearchRepository: SavedSearchRepository,
    savedStateHandle: SavedStateHandle,
) : BaseBrowseViewModel(server, tags, postRepository, serverRepository, favoriteRepository,savedSearchRepository,savedStateHandle) {}