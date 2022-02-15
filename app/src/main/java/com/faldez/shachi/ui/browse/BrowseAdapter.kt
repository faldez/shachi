package com.faldez.shachi.ui.browse

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.model.Post


class BrowseAdapter(
    private val gridMode: String,
    private val quality: String,
    private val onClick: (Int) -> Unit,
) :
    PagingDataAdapter<Post, BrowseItemViewHolder>(POST_COMPARATOR) {
    private lateinit var binding: BrowseItemViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        binding =
            BrowseItemViewHolder(PostCardItemBinding.inflate(inflater, parent, false),
                gridMode,
                quality,
                onClick)
        return binding
    }

    fun getPosition(): Int {
        return binding.bindingAdapterPosition
    }

    override fun onBindViewHolder(holder: BrowseItemViewHolder, position: Int) {
        val post = getItem(position)

        post?.let { holder.bind(post) }
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

class BrowseItemViewHolder(
    val binding: PostCardItemBinding,
    private val gridMode: String,
    val quality: String,
    val onClick: (Int) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        val imageView = binding.imageView

        val previewWidth = post.previewWidth ?: 250
        val previewHeight = if (gridMode == "staggered") {
            post.previewHeight
                ?: (previewWidth * (post.height.toFloat() / post.width.toFloat())).toInt()
        } else {
            previewWidth
        }

        val url = when (quality) {
            "sample" -> post.sampleUrl ?: post.previewUrl
            "original" -> post.fileUrl
            else -> post.previewUrl ?: post.sampleUrl
        } ?: post.fileUrl

        Glide.with(imageView.context).load(url)
            .transition(withCrossFade(factory))
            .placeholder(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(previewWidth,
                    previewHeight,
                    Bitmap.Config.ARGB_8888))).override(previewWidth, previewHeight)
            .into(imageView)

        binding.root.isChecked = post.favorite

        binding.root.setOnClickListener { _ ->
            onClick(bindingAdapterPosition)
        }
    }
}
