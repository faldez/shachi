package com.faldez.shachi.service

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
            val file = downloadDir?.let {
                DocumentFile.fromTreeUri(applicationContext, Uri.parse(it))
                    ?.createFile("image/*", fileUri.lastPathSegment!!)
            }

            URL(fileUrl).openStream().use { input ->
                contentResolver.openOutputStream(file!!.uri)
                    ?.use { output ->
                        input.copyTo(output)
                    }
            }

            contentResolver.openInputStream(file!!.uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                if (bitmap != null) {
                    showNotification(R.string.download_finished, bitmap)
                }
            }

            stopSelf(msg.arg1)
        }

        private fun showNotification(text: Int, bitmap: Bitmap) {
            val builder = NotificationCompat.Builder(applicationContext, "DOWNLOAD")
                .setSmallIcon(R.drawable.ic_baseline_download_24)
                .setContentTitle(resources.getText(text))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
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