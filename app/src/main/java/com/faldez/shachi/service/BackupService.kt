package com.faldez.shachi.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.faldez.shachi.R
import com.faldez.shachi.data.api.BooruApiImpl
import com.faldez.shachi.data.backup.*
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.repository.ServerRepository
import com.faldez.shachi.data.repository.blacklist_tag.BlacklistTagRepository
import com.faldez.shachi.data.repository.blacklist_tag.BlacklistTagRepositoryImpl
import com.faldez.shachi.data.repository.favorite.FavoriteRepository
import com.faldez.shachi.data.repository.favorite.FavoriteRepositoryImpl
import com.faldez.shachi.data.repository.saved_search.SavedSearchRepository
import com.faldez.shachi.data.repository.saved_search.SavedSearchRepositoryImpl
import com.faldez.shachi.data.repository.search_history.SearchHistoryRepository
import com.faldez.shachi.data.repository.search_history.SearchHistoryRepositoryImpl
import com.faldez.shachi.data.repository.server.ServerRepositoryImpl
import com.faldez.shachi.data.repository.tag.TagRepository
import com.faldez.shachi.data.repository.tag.TagRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.zip.GZIPOutputStream

class BackupService : Service() {
    private lateinit var serviceLooper: Looper
    private lateinit var serviceHandler: ServiceHandler

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // repositories
    private lateinit var serverRepository: ServerRepository
    private lateinit var blacklistTagRepository: BlacklistTagRepository
    private lateinit var savedSearchRepository: SavedSearchRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var searchHistoryRepository: SearchHistoryRepository
    private lateinit var tagRepository: TagRepository

    companion object {
        const val TAG = "BackupService"
        const val ARG = "BackupServiceStartArguments"
    }

    override fun onCreate() {
        HandlerThread(ARG, Process.THREAD_PRIORITY_BACKGROUND).also {
            it.start()

            serviceLooper = it.looper
            serviceHandler = ServiceHandler(looper = it.looper)

            val db = AppDatabase.build(applicationContext)
            serverRepository = ServerRepositoryImpl(db)
            blacklistTagRepository = BlacklistTagRepositoryImpl(db)
            savedSearchRepository = SavedSearchRepositoryImpl(db)
            favoriteRepository = FavoriteRepositoryImpl(db)
            searchHistoryRepository = SearchHistoryRepositoryImpl(db)
            tagRepository = TagRepositoryImpl(BooruApiImpl(), db)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceHandler.obtainMessage().apply {
            arg1 = startId
            data = intent?.extras

            serviceHandler.sendMessage(this)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            msg.data.getParcelable<Uri>("file_uri")?.let { fileUri ->
                val notificationManager = NotificationManagerCompat.from(applicationContext)

                val builder = NotificationCompat.Builder(applicationContext, "system")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(resources.getText(R.string.backup))
                    .setContentText("Backup success")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                scope.launch {
                    try {
                        val servers = getServerBackup()
                        val favorites = getFavoritesBackup()
                        val savedSearches = getSavedSearchBackup()
                        val searchHistories = getSearchHistoryBackup()

                        val backup = Backup(
                            servers = servers,
                            favorites = favorites,
                            savedSearches = savedSearches,
                            searchHistories = searchHistories
                        )

                        contentResolver.openOutputStream(fileUri)?.use { output ->
                            GZIPOutputStream(output).use { gzOutput ->
                                val bytes = ProtoBuf.encodeToByteArray(backup)

                                gzOutput.write(bytes)


                                notificationManager.notify(0, builder.build())
                            }
                        }
                    } catch (e: Exception) {
                        builder.setContentText("$e")
                        notificationManager.notify(0, builder.build())
                    }
                }
            }

            stopSelf(msg.arg1)
        }

        suspend fun getServerBackup(): List<ServerBackup>? {
            return serverRepository.getAllServers()?.map {
                ServerBackup(
                    serverId = it.serverId,
                    type = it.type,
                    title = it.title,
                    url = it.url,
                    username = it.username,
                    password = it.password,
                    blacklistedTags = it.blacklistedTags,
                    selected = it.selected
                )
            }
        }

        suspend fun getSavedSearchBackup(): List<SavedSearchBackup> {
            return savedSearchRepository.getAll().map {
                SavedSearchBackup(
                    id = it.savedSearchId,
                    tags = it.tags,
                    title = it.savedSearchTitle,
                    serverId = it.serverId,
                    order = it.order,
                    dateAdded = it.dateAdded
                )
            }
        }

        suspend fun getSearchHistoryBackup(): List<SearchHistoryBackup>? {
            return searchHistoryRepository.getAll()?.map {
                SearchHistoryBackup(
                    id = it.searchHistoryId,
                    serverId = it.serverId,
                    tags = it.tags,
                    createdAt = it.createdAt
                )
            }
        }

        suspend fun getFavoritesBackup(): List<FavoriteBackup> {
            return favoriteRepository.getAll().map {
                FavoriteBackup(
                    height = it.height,
                    width = it.width,
                    score = it.score,
                    fileUrl = it.fileUrl,
                    parentId = it.parentId,
                    sampleUrl = it.sampleUrl,
                    sampleWidth = it.sampleWidth,
                    sampleHeight = it.sampleHeight,
                    previewUrl = it.previewUrl,
                    previewWidth = it.previewWidth,
                    previewHeight = it.previewHeight,
                    rating = it.rating,
                    tags = it.tags,
                    postId = it.postId,
                    serverId = it.serverId,
                    change = it.change,
                    md5 = it.md5,
                    creatorId = it.creatorId,
                    hasChildren = it.hasChildren,
                    createdAt = it.createdAt,
                    status = it.status,
                    source = it.source,
                    hasNotes = it.hasNotes,
                    hasComments = it.hasComments,
                    postUrl = it.postUrl,
                    dateAdded = it.dateAdded
                )
            }
        }
    }
}