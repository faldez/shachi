package com.faldez.shachi.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.faldez.shachi.R
import com.faldez.shachi.data.backup.Backup
import com.faldez.shachi.data.database.AppDatabase
import com.faldez.shachi.data.model.*
import com.faldez.shachi.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.zip.GZIPInputStream

class RestoreService : Service() {
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
        const val TAG = "RestoreService"
        const val ARG = "RestoreServiceStartArguments"
    }

    override fun onCreate() {
        HandlerThread(ARG, Process.THREAD_PRIORITY_BACKGROUND).also {
            it.start()

            serviceLooper = it.looper
            serviceHandler = ServiceHandler(looper = it.looper)

            val db = AppDatabase.build(applicationContext)
            serverRepository = ServerRepository(db)
            blacklistTagRepository = BlacklistTagRepository(db)
            savedSearchRepository = SavedSearchRepository(db)
            favoriteRepository = FavoriteRepository(db)
            searchHistoryRepository = SearchHistoryRepository(db)
            tagRepository = TagRepository(BooruServiceImpl(), db)
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
                    .setContentTitle(resources.getText(R.string.restore))
                    .setPriority(NotificationCompat.PRIORITY_LOW)

                scope.launch {
                    try {
                        contentResolver.openInputStream(fileUri)?.use { output ->
                            GZIPInputStream(output).use { gzInput ->
                                builder.setContentText("Restoring...")
                                notificationManager.notify(0, builder.build())

                                val bytes = gzInput.readBytes()

                                val backup: Backup = ProtoBuf.decodeFromByteArray(bytes)
                                restore(backup)

                                builder.setContentText("Restore success")
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

        suspend fun restore(data: Backup) {
            try {
                val serverIdMap: MutableMap<Int, Int> = mutableMapOf()
                val blacklistedTagCrossRefs: MutableList<ServerBlacklistedTagCrossRef> =
                    mutableListOf()
                data.servers?.forEach { serverToRestore ->
                    var server = serverRepository.getServerByUrl(serverToRestore.url)
                    if (server == null) {
                        serverRepository.insert(Server(
                            serverId = 0,
                            type = serverToRestore.type,
                            title = serverToRestore.title,
                            url = serverToRestore.url,
                            username = serverToRestore.username,
                            password = serverToRestore.password,
                        ))
                        server = serverRepository.getServerByUrl(serverToRestore.url)!!
                    }

                    serverIdMap[serverToRestore.serverId] = server.serverId

                    if (server.type == ServerType.Moebooru) {
                        tagRepository.getTagsSummary(Action.GetTagsSummary(server = server.toServer()))
                            ?.also {
                                tagRepository.insertTags(it)
                            }
                    }

                    //restore blacklist tag
                    serverToRestore.blacklistedTags?.split(",")
                        ?.forEach { tags ->
                            val blacklistTagsToRestore =
                                blacklistTagRepository.getByTags(tags)
                                    ?: BlacklistedTag(blacklistedTagId = blacklistTagRepository.insertBlacklistedTag(
                                        BlacklistedTag(tags = tags)).toInt(),
                                        tags = tags)

                            blacklistedTagCrossRefs.add(ServerBlacklistedTagCrossRef(serverId = server.serverId,
                                blacklistedTagId = blacklistTagsToRestore.blacklistedTagId))
                        }
                }

                // restore blacklist crossref
                blacklistTagRepository.insertBlacklistedTag(blacklistedTagCrossRefs)

                // restore saved search
                data.savedSearches?.forEach {
                    serverIdMap[it.serverId]?.let { serverId ->
                        savedSearchRepository.insert(tags = it.tags,
                            title = it.title,
                            serverId = serverId)
                    }
                }

                // restore favorites
                data.favorites?.forEach {
                    serverIdMap[it.serverId]?.let { serverId ->
                        favoriteRepository.insert(Post(
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
                            serverId = serverId,
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
                        ))
                    }
                }

                // restore search histories
                data.searchHistories?.forEach {
                    serverIdMap[it.serverId]?.let { serverId ->
                        searchHistoryRepository.insert(SearchHistory(
                            serverId = serverId,
                            tags = it.tags,
                            createdAt = it.createdAt
                        ))
                    }
                }
            } catch (e: Error) {
                Log.d("ServerEditViewModel/test", "$e")
            }
        }
    }
}