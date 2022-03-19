package com.faldez.shachi.data.repository.tag

import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.service.BooruService

class TagRepositoryImpl(override val service: BooruService, override val db: AppDatabase) :
    TagRepository