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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.faldez.shachi.databinding.PostSlideItemBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.util.glide.GlideApp
import com.faldez.shachi.util.glide.GlideModule


class PostSlideAdapter(
    private val onTap: () -> Boolean,
) :
    PagingDataAdapter<Post, PostSlideViewHolder>(POST_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostSlideViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = PostSlideItemBinding.inflate(inflater, parent, false)
        return PostSlideViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostSlideViewHolder, position: Int) {
        val post = getItem(position)

        post?.let {
            val postImageView = holder.binding.postImageView
            postImageView.setOnViewTapListener { view, x, y -> onTap() }
            holder.binding.postLoadingIndicator.isIndeterminate = true
            GlideModule.setOnProgress(it.fileUrl,
                onProgress = { bytesRead, totalContentLength, done ->
                    holder.binding.postLoadingIndicator.max = totalContentLength.toInt()
                    holder.binding.postLoadingIndicator.progress = bytesRead.toInt()
                    holder.binding.postLoadingIndicator.isIndeterminate = false
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
                        holder.binding.postLoadingIndicator.isVisible = false
                        holder.binding.loadingCard.isVisible = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        holder.binding.postLoadingIndicator.isVisible = false
                        holder.binding.loadingCard.isVisible = false
                        return false
                    }

                })
                .into(postImageView)
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

class PostSlideViewHolder(val binding: PostSlideItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
