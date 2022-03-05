package com.faldez.shachi.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.RequestOptions
import com.faldez.shachi.R
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.Rating
import com.faldez.shachi.data.preference.GridMode
import com.faldez.shachi.data.preference.Quality

fun bindPostImagePreview(
    imageView: ImageView,
    item: Post?,
    gridMode: GridMode,
    quality: Quality,
    hideQuestionable: Boolean,
    hideExplicit: Boolean,
) {
    val previewWidth: Int = item?.previewWidth ?: 250
    val previewHeight: Int = if (item != null) {
        if (gridMode == GridMode.Staggered) {
            item.previewHeight
                ?: (previewWidth * (item.height.toFloat() / item.width.toFloat())).toInt()
        } else {
            previewWidth
        }
    } else {
        250
    }

    var glide = if (item == null) {
        val drawable = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
        Glide.with(imageView.context).load(drawable)
    } else if (hideQuestionable && item.rating == Rating.Questionable || hideExplicit && item.rating == Rating.Explicit) {
        val drawable = ResourcesCompat.getDrawable(imageView.resources,
            R.drawable.nsfw_placeholder,
            null)
            ?.toBitmap(previewWidth, previewHeight, Bitmap.Config.RGB_565)

        Glide.with(imageView.context).load(drawable)
    } else {
        val url = when (quality) {
            Quality.Sample -> item.sampleUrl ?: item.previewUrl
            Quality.Original -> item.fileUrl
            else -> item.previewUrl ?: item.sampleUrl
        } ?: item.fileUrl

        Glide.with(imageView.context).load(url)
            .placeholder(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(previewWidth,
                    previewHeight,
                    Bitmap.Config.RGB_565)))
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        glide = glide.apply(RequestOptions().set(Downsampler.ALLOW_HARDWARE_CONFIG, true)
            .format(DecodeFormat.PREFER_RGB_565))
    }

    glide.diskCacheStrategy(DiskCacheStrategy.ALL).override(previewWidth, previewHeight)
        .into(imageView)
}