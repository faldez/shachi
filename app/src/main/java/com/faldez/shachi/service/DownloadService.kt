package com.faldez.shachi.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import com.faldez.shachi.R
import com.faldez.shachi.model.Post
import com.faldez.shachi.util.MimeUtil
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

class DownloadService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val post = msg.data.getParcelable("post") as Post?
            val fileUrl = post!!.fileUrl
            val fileUri = Uri.parse(fileUrl)

            val downloadDir = msg.data.getString("download_dir")
            val mime = MimeUtil.getMimeTypeFromUrl(fileUrl)
            downloadDir?.let {
                DocumentFile.fromTreeUri(applicationContext, Uri.parse(it))
                    ?.createFile(mime ?: "image/*", fileUri.lastPathSegment!!)
            }?.let { file ->
                val builder = NotificationCompat.Builder(applicationContext, "DOWNLOAD")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(resources.getText(R.string.download))
                    .setContentText(resources.getText(R.string.downloading))
                    .setPriority(NotificationCompat.PRIORITY_LOW)

                val connection: URLConnection
                NotificationManagerCompat.from(applicationContext).apply {
                    var input: InputStream? = null
                    var output: OutputStream? = null

                    try {
                        val url = URL(fileUrl)
                        connection = url.openConnection()
                        connection.connect()

                        val fileLength = connection.contentLength
                        input = connection.getInputStream()
                        output = contentResolver.openOutputStream(file.uri)

                        val determinate = fileLength > 0
                        builder.setProgress(100, 0, !determinate)
                        notify(post.postId, builder.build())

                        var total = 0
                        val data = ByteArray(1024)

                        do {
                            val count = input.read(data)
                            if (count > 0) {
                                total += count
                                if (fileLength > 0) {
                                    builder.setProgress(100,
                                        (total * 100 / fileLength),
                                        !determinate)
                                    notify(post.postId, builder.build())
                                }
                                output?.write(data, 0, count)
                            }
                        } while (count != -1)
                    } catch (e: Exception) {
                        Log.e("ServiceHandler", "$e")
                    } finally {
                        output?.close()
                        input?.close()
                    }

                    val downloaded = contentResolver.openInputStream(file.uri)
                    val bitmap = BitmapFactory.decodeStream(downloaded)

                    val intent = Intent(Intent.ACTION_VIEW, file.uri).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                    val contentIntent = PendingIntent.getActivity(applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE)
                    builder.setContentText(resources.getString(R.string.download_finished))
                        .setProgress(0, 0, false)
                        .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                        .setContentIntent(contentIntent)
                    notify(post.postId, builder.build())
                }
            }

            stopSelf(msg.arg1)
        }

        private fun publishProgress(progress: Int) {

        }

        private fun showNotification(text: Int, imageUri: Uri, preview: Bitmap? = null) {
            val intent = Intent(Intent.ACTION_VIEW, imageUri).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            val contentIntent = PendingIntent.getActivity(applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE)
            var builder = NotificationCompat.Builder(applicationContext, "DOWNLOAD")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(resources.getText(text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
            if (preview != null) {
                builder = builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(preview))
            }
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(0, builder.build())
            }
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