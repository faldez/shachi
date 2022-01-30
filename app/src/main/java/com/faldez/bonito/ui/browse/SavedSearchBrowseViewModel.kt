package com.faldez.bonito.ui.browse

import androidx.lifecycle.SavedStateHandle
import com.faldez.bonito.data.FavoriteRepository
import com.faldez.bonito.data.PostRepository
import com.faldez.bonito.data.SavedSearchRepository
import com.faldez.bonito.data.ServerRepository
import com.faldez.bonito.model.Server
import com.faldez.bonito.ui.base.BaseBrowseViewModel

class SavedSearchBrowseViewModel constructor(
    server: Server?,
    tags: String?,
    postRepository: PostRepository,
    serverRepository: ServerRepository,
    favoriteRepository: FavoriteRepository,
    savedSearchRepository: SavedSearchRepository,
    savedStateHandle: SavedStateHandle,
) : BaseBrowseViewModel(server, tags, postRepository, serverRepository, favoriteRepository,savedSearchRepository,savedStateHandle) {}