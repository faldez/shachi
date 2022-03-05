package com.faldez.shachi.ui.post_slide

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostSlideItemBinding
import com.faldez.shachi.databinding.PostSlideItemVideoBinding
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.util.MimeUtil
import com.faldez.shachi.util.glide.GlideApp
import com.faldez.shachi.util.glide.GlideModule
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem


class PostSlideAdapter(
    val quality: String,
    private val onTap: () -> Unit,
) :
    PagingDataAdapter<Post, PostSlideViewHolder>(POST_COMPARATOR) {

    fun setPostQuality(position: Int, quality: String) {
        val item = getItem(position)
        if (item != null) {
            item.quality = quality
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostSlideViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            viewTypeImage -> {
                val binding = PostSlideItemBinding.inflate(inflater, parent, false)
                PostSlideImageViewHolder(binding, quality, onTap)
            }
            viewTypeVideo -> {
                val binding = PostSlideItemVideoBinding.inflate(inflater, parent, false)
                PostSlideVideoViewHolder(binding, onTap)
            }
            else -> throw IllegalAccessException()
        }
    }

    override fun onBindViewHolder(holder: PostSlideViewHolder, position: Int) {
        if (holder is PostSlideImageViewHolder) {
            holder.bind(getItem(position))
        }
    }

    fun getPostItem(position: Int): Post? {
        return try {
            getItem(position)
        } catch (exception: IndexOutOfBoundsException) {
            null
        }
    }

    fun setFavorite(position: Int, favorite: Boolean) {
        val item = getItem(position)
        item?.let {
            it.favorite = favorite
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        val mime = item?.fileUrl?.let {
            MimeUtil.getMimeTypeFromUrl(it)
        }
        return if (mime?.startsWith("video") == true) {
            viewTypeVideo
        } else {
            viewTypeImage
        }
    }

    override fun onViewDetachedFromWindow(holder: PostSlideViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is PostSlideVideoViewHolder) {
            holder.pausePlayer()
            holder.releasePlayer()
        }
    }

    override fun onViewAttachedToWindow(holder: PostSlideViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is PostSlideVideoViewHolder) {
            getPostItem(holder.bindingAdapterPosition)?.let { holder.bind(it) }
            holder.startPlayer()
        }
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.postId == newItem.postId && oldItem.serverId == newItem.serverId


            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem

        }
    }
}

abstract class PostSlideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: Post?)
}

class PostSlideImageViewHolder(
    private val binding: PostSlideItemBinding,
    private val quality: String,
    private val onTap: () -> Unit,
) :
    PostSlideViewHolder(binding.root) {
    override fun bind(item: Post?) {
        val postImageView = binding.postImageView
        postImageView.setOnViewTapListener { _, _, _ -> onTap() }
        binding.postLoadingIndicator.isIndeterminate = true

        if (item != null) {
            var url = when (item.quality ?: quality) {
                "sample" -> {
                    item.quality = "sample"
                    item.sampleUrl
                }
                "original" -> {
                    item.quality = "original"
                    item.fileUrl
                }
                else -> {
                    item.quality = "preview"
                    item.previewUrl
                }
            }

            if (url == null) {
                item.quality = "original"
                url = item.fileUrl
            }

            Log.d("PostSlideImageViewHolder", "bind ${item.quality} $url")

            GlideModule.setOnProgress(url,
                onProgress = { bytesRead, totalContentLength, done ->
                    binding.postLoadingIndicator.max = totalContentLength.toInt()
                    binding.postLoadingIndicator.progress = bytesRead.toInt()
                    binding.postLoadingIndicator.isIndeterminate = false
                })

            var glide = GlideApp.with(postImageView.context).load(url)
                .thumbnail(GlideApp.with(postImageView.context).load(item.previewUrl))
                .timeout(3000)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        binding.postLoadingIndicator.isVisible = false
                        binding.loadingCard.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        binding.postLoadingIndicator.isVisible = false
                        binding.loadingCard.isVisible = false
                        return false
                    }
                })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                glide = glide.apply(RequestOptions().set(Downsampler.ALLOW_HARDWARE_CONFIG, true))
            }
            glide.into(postImageView)
        } else {
            GlideApp.with(postImageView.context).load(BitmapDrawable(postImageView.resources,
                Bitmap.createBitmap(150,
                    150,
                    Bitmap.Config.ARGB_8888))).into(postImageView)
        }
    }
}


class PostSlideVideoViewHolder(
    private val binding: PostSlideItemVideoBinding,
    private val onTap: () -> Unit,
) :
    PostSlideViewHolder(binding.root) {
    private lateinit var player: ExoPlayer

    override fun bind(item: Post?) {
        player = ExoPlayer.Builder(binding.root.context).build()
        binding.videoPlayerView.player = player
        binding.videoPlayerView.setOnClickListener {
            onTap()
        }
        val volumeToggle = binding.videoPlayerView.findViewById<ImageButton>(R.id.volume_toggle)
        volumeToggle.setOnClickListener {
            Log.d("PostSlideVideoViewHolder", "volume toggled")
            if (player.volume == 0f) {
                player.volume = 1f
            } else if (player.volume == 1f) {
                player.volume = 0f
            }
            volumeToggle.isSelected = player.volume == 1f
        }
        item?.fileUrl?.let {
            val mediaItem = MediaItem.fromUri(it)
            player.setMediaItem(mediaItem)
        }
        player.prepare()
        player.volume = 0f
        volumeToggle.isSelected = false
    }

    fun startPlayer() {
        player.playWhenReady = true
        player.playbackState
    }

    fun pausePlayer() {
        player.playWhenReady = false
        player.playbackState
    }

    fun releasePlayer() {
        player.playWhenReady = false
        player.release()
        player.stop()
        player.clearMediaItems()
    }
}

const val viewTypeImage = 0
const val viewTypeVideo = 1
