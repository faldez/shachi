package com.faldez.shachi.ui.post_slide

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.faldez.shachi.databinding.PostSlideItemBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.util.glide.GlideApp
import com.faldez.shachi.util.glide.GlideModule


class PostSlideAdapter(
    private val quality: String,
    private val onTap: () -> Unit,
) :
    PagingDataAdapter<Post, PostSlideViewHolder>(POST_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostSlideViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = PostSlideItemBinding.inflate(inflater, parent, false)
        return PostSlideViewHolder(binding, quality, onTap)
    }

    override fun onBindViewHolder(holder: PostSlideViewHolder, position: Int) {
        val post = getItem(position)

        post?.let {
            holder.bind(it)
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
            notifyItemChanged(position)
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

class PostSlideViewHolder(
    private val binding: PostSlideItemBinding,
    private val quality: String,
    private val onTap: () -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(it: Post) {
        val postImageView = binding.postImageView
        postImageView.setOnViewTapListener { view, x, y -> onTap() }
        binding.postLoadingIndicator.isIndeterminate = true

        val url = when (quality) {
            "sample" -> it.sampleUrl ?: it.previewUrl
            "original" -> it.fileUrl
            else -> it.previewUrl ?: it.sampleUrl
        } ?: it.fileUrl

        GlideModule.setOnProgress(url,
            onProgress = { bytesRead, totalContentLength, done ->
                binding.postLoadingIndicator.max = totalContentLength.toInt()
                binding.postLoadingIndicator.progress = bytesRead.toInt()
                binding.postLoadingIndicator.isIndeterminate = false
            })
        GlideApp.with(postImageView.context).load(it.fileUrl)
            .thumbnail(GlideApp.with(postImageView.context).load(it.previewUrl))
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
            .into(postImageView)
    }
}
