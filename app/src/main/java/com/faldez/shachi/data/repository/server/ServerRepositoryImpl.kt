package com.faldez.shachi.data.repository.server

import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.repository.ServerRepository

class ServerRepositoryImpl(override val db: AppDatabase) : ServerRepository