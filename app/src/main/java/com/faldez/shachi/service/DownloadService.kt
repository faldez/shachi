package com.faldez.shachi.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import com.faldez.shachi.R
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.util.MimeUtil
import java.net.URL

class DownloadService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    companion object {
        const val TAG = "ServiceHandler"
        const val PROGRESS_MAX = 100
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val post = msg.data.getParcelable("post") as Post?
            val fileUrl = post!!.fileUrl
            val fileUri = Uri.parse(fileUrl)

            msg.data.getString("download_dir")?.let {
                val mime = MimeUtil.getMimeTypeFromUrl(fileUrl)
                DocumentFile.fromTreeUri(applicationContext, Uri.parse(it))
                    ?.createFile(mime ?: "image/*", fileUri.lastPathSegment!!)
            }?.let { file ->
                val builder = NotificationCompat.Builder(applicationContext, "download")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(resources.getText(R.string.download))
                    .setContentText(resources.getText(R.string.downloading))
                    .setPriority(NotificationCompat.PRIORITY_LOW)

                val notificationManager = NotificationManagerCompat.from(applicationContext)
                try {
                    val connection = URL(fileUrl).openConnection()
                    val fileLength = connection.contentLength
                    connection.getInputStream()?.use { input ->
                        contentResolver.openOutputStream(file.uri)?.use { output ->
                            val indeterminate = fileLength <= 0
                            builder.setProgress(PROGRESS_MAX, 0, indeterminate)
                            notificationManager.notify(post.postId, builder.build())

                            var total = 0
                            val data = ByteArray(4096)
                            var lastNotify = 0
                            while (true) {
                                val count = input.read(data)
                                if (count == -1) {
                                    break
                                }

                                total += count
                                if (fileLength > 0) {
                                    val progress = (total * 100 / fileLength)
                                    if (progress - lastNotify >= 10) {
                                        builder.setProgress(PROGRESS_MAX, progress, indeterminate)
                                        notificationManager.notify(post.postId, builder.build())
                                        lastNotify = progress

                                        Log.i(TAG, "notify: $progress")
                                    }
                                    Log.i(TAG,
                                        "downloading ${post.postId}: $progress% from $fileLength")
                                }
                                output.write(data, 0, count)
                            }
                        }
                    }

                    val contentIntent = Intent(Intent.ACTION_VIEW, file.uri).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }.let {
                        PendingIntent.getActivity(applicationContext,
                            0,
                            it,
                            PendingIntent.FLAG_IMMUTABLE)
                    }

                    contentResolver.openInputStream(file.uri)?.use {
                        val bitmap = BitmapFactory.decodeStream(it)
                        Log.i(TAG, "download ${post.postId} finished: ${bitmap != null}")

                        builder.setContentText(resources.getString(R.string.download_finished))
                            .setProgress(0, 0, false)
                            .setStyle(NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap))
                            .setContentIntent(contentIntent)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                        notificationManager.notify(post.postId, builder.build())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "$e")
                    builder.setContentText("Download error: $e")
                        .setProgress(0, 0, false)
                    notificationManager.notify(post.postId, builder.build())
                }
            }

            stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId
            msg.data = intent?.extras
            serviceHandler?.sendMessage(msg)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}