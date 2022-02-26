package com.faldez.shachi.ui.favorite

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.util.glide.GlideApp

class FavoriteAdapter(
    private val gridMode: String,
    private val quality: String,
    private val hideQuestionable: Boolean,
    private val hideExplicit: Boolean,
    private val onClick: (Int) -> Unit,
) :
    PagingDataAdapter<Post, BrowseItemViewHolder>(POST_COMPARATOR) {
    private lateinit var binding: BrowseItemViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        binding = BrowseItemViewHolder(PostCardItemBinding.inflate(inflater, parent, false))
        return binding
    }

    fun getPosition(): Int {
        return binding.bindingAdapterPosition
    }

    override fun onBindViewHolder(holder: BrowseItemViewHolder, position: Int) {
        val post = getItem(position)
        val imageView = holder.binding.imageView

        if (post != null) {
            val previewWidth = post.previewWidth ?: 150
            val previewHeight = if (gridMode == "staggered") {
                (previewWidth * (post.height.toFloat() / post.width.toFloat())).toInt()
            } else {
                previewWidth
            }
            if (hideQuestionable && post.rating == Rating.Questionable || hideExplicit && post.rating == Rating.Explicit) {
                val drawable = ResourcesCompat.getDrawable(holder.binding.root.resources,
                    R.drawable.nsfw_placeholder,
                    null)
                    ?.toBitmap(previewWidth, previewHeight)

                Glide.with(imageView.context).load(drawable)
                    .into(imageView)
            } else {
                val url = when (quality) {
                    "sample" -> post.sampleUrl ?: post.previewUrl
                    "original" -> post.fileUrl
                    else -> post.previewUrl ?: post.sampleUrl
                } ?: post.fileUrl

                Glide.with(imageView.context).load(url)
                    .placeholder(BitmapDrawable(imageView.resources,
                        Bitmap.createBitmap(previewWidth,
                            previewHeight,
                            Bitmap.Config.ARGB_8888)))
                    .override(previewWidth, previewHeight)
                    .into(imageView)
            }

            holder.binding.root.setOnClickListener { _ ->
                onClick(position)
            }
        } else {
            GlideApp.with(imageView.context).load(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(150,
                    150,
                    Bitmap.Config.ARGB_8888))).into(imageView)
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

class BrowseItemViewHolder(val binding: PostCardItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
