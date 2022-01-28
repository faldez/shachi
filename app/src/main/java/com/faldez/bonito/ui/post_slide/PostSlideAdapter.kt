package com.faldez.bonito.ui.post_slide

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.faldez.bonito.GlideApp
import com.faldez.bonito.databinding.PostSlideItemBinding
import com.faldez.bonito.model.Post


class PostSlideAdapter(private val onTap: () -> Unit, private val onLoadEnd: () -> Unit) :
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
            postImageView.setOnViewTapListener { view, x, y -> onTap()  }
            GlideApp.with(postImageView.context).load(it.fileUrl)
                .thumbnail(Glide.with(postImageView.context).load(it.previewUrl))
                .timeout(3000)
                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        onLoadEnd()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean,
                    ): Boolean {
                        onLoadEnd()
                        return false
                    }

                })
                .into(postImageView)
        }
    }

    fun getPostItem(position: Int): Post? {
        return getItem(position)
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
                oldItem.postId == newItem.postId && oldItem.serverUrl == newItem.serverUrl


            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem

        }
    }
}

class PostSlideViewHolder(val binding: PostSlideItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
