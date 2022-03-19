package com.faldez.shachi.data.repository.tag

import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.api.BooruApi

class TagRepositoryImpl(override val booruApi: BooruApi, override val db: AppDatabase) :
    TagRepository