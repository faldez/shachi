package com.faldez.sachi.ui.browse

import androidx.lifecycle.SavedStateHandle
import com.faldez.sachi.data.FavoriteRepository
import com.faldez.sachi.data.PostRepository
import com.faldez.sachi.data.SavedSearchRepository
import com.faldez.sachi.data.ServerRepository
import com.faldez.sachi.model.Server
import com.faldez.sachi.ui.base.BaseBrowseViewModel

class BrowseViewModel constructor(
    server: Server?,
    tags: String?,
    postRepository: PostRepository,
    serverRepository: ServerRepository,
    favoriteRepository: FavoriteRepository,
    savedSearchRepository: SavedSearchRepository,
    savedStateHandle: SavedStateHandle,
) : BaseBrowseViewModel(server, tags, postRepository, serverRepository, favoriteRepository,savedSearchRepository,savedStateHandle) {}