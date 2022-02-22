package com.faldez.shachi.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import com.faldez.shachi.R
import com.faldez.shachi.model.Post
import com.faldez.shachi.util.MimeUtil
import java.net.URL

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
                URL(fileUrl).openStream().use { input ->
                    contentResolver.openOutputStream(file.uri)
                        ?.use { output ->
                            input.copyTo(output)
                        }
                }

                contentResolver.openInputStream(file.uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    showNotification(R.string.download_finished, file.uri, bitmap)
                }
            }

            stopSelf(msg.arg1)
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