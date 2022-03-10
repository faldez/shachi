package com.faldez.shachi.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.RequestOptions
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.Rating
import com.faldez.shachi.data.preference.GridMode
import com.faldez.shachi.data.preference.Quality

fun bindPostImagePreview(
    context: Context,
    target: ImageView,
    item: Post?,
    gridMode: GridMode,
    quality: Quality,
    hideQuestionable: Boolean,
    hideExplicit: Boolean,
) {
    val previewWidth = 250
    val previewHeight: Int = if (item != null && gridMode == GridMode.Staggered) {
        (previewWidth * (item.height.toFloat() / item.width.toFloat())).toInt()
    } else {
        250
    }

    var options = RequestOptions()
        .format(DecodeFormat.PREFER_RGB_565)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        options = options.set(Downsampler.ALLOW_HARDWARE_CONFIG, true)
    }


    if (item != null) {
        if (hideQuestionable && item.rating == Rating.Questionable || hideExplicit && item.rating == Rating.Explicit) {
            Glide.with(context).load(ColorDrawable(Color.RED).toBitmap(previewWidth,
                previewHeight,
                Bitmap.Config.RGB_565))
        } else {
            val url = when (quality) {
                Quality.Sample -> item.sampleUrl ?: item.previewUrl
                Quality.Original -> item.fileUrl
                else -> item.previewUrl ?: item.sampleUrl
            } ?: item.fileUrl

            Glide.with(context).load(url)
                .placeholder(BitmapDrawable(context.resources,
                    Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ALPHA_8)))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
        }
    } else {
        Glide.with(context).load(ColorDrawable(Color.GRAY).toBitmap(previewWidth,
            previewHeight,
            Bitmap.Config.RGB_565))
    }
        .apply(options)
        .override(previewWidth, previewHeight)
        .into(target)
}