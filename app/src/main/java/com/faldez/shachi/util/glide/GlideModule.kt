package com.faldez.shachi.util.glide

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.*
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

@GlideModule
class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val client = OkHttpClient.Builder().addNetworkInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val listener = DispatchProgressListener()
            response.newBuilder().body(response.body
                ?.let { ProgressResponseBody(request.url.toString(), it, listener) })
                .build()
        }.build()
        registry.replace(GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(client))
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    internal class ProgressResponseBody(
        private val url: String,
        private val responseBody: ResponseBody,
        private val progressListener: ProgressListener,
    ) : ResponseBody() {
        private val bufferedSource: BufferedSource by lazy {
            source(responseBody.source()).buffer()
        }

        override fun contentType(): MediaType? {
            return responseBody.contentType()
        }

        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            return bufferedSource
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead = super.read(sink, byteCount)
                    totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                    progressListener.update(
                        url,
                        totalBytesRead,
                        responseBody.contentLength(),
                        bytesRead == -1L)
                    return bytesRead
                }
            }
        }

    }

    internal interface ProgressListener {
        fun update(url: String, bytesRead: Long, contentLength: Long, done: Boolean)
    }

    internal class DispatchProgressListener : ProgressListener {
        companion object {
            val LISTENERS = ConcurrentHashMap<String, (Long, Long, Boolean) -> Unit>()

            fun setOnProgress(url: String, onProgress: (Long, Long, Boolean) -> Unit) {
                LISTENERS.put(url, onProgress)
            }
        }

        private val handler = Handler(Looper.getMainLooper())

        override fun update(url: String, bytesRead: Long, contentLength: Long, done: Boolean) {
            LISTENERS.get(url)?.let { onProgress ->
                handler.post {
                    onProgress(bytesRead, contentLength, done)
                }
            }
            if (done) LISTENERS.remove(url)
        }
    }

    companion object {
        fun setOnProgress(url: String, onProgress: (Long, Long, Boolean) -> Unit) {
            DispatchProgressListener.setOnProgress(url, onProgress)
        }
    }
}