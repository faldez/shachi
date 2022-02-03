package com.faldez.shachi.ui.browse

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.model.Post


class BrowseAdapter(private val onClick: (Int) -> Unit) :
    PagingDataAdapter<Post, BrowseItemViewHolder>(POST_COMPARATOR) {
    private lateinit var binding: BrowseItemViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        binding =
            BrowseItemViewHolder(PostCardItemBinding.inflate(inflater, parent, false), onClick)
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

class BrowseItemViewHolder(val binding: PostCardItemBinding, val onClick: (Int) -> Unit) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        val imageView = binding.imageView
        Glide.with(imageView.context).load(post.previewUrl)
            .transition(withCrossFade(factory))
            .placeholder(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(post.previewWidth!!,
                    post.previewHeight!!,
                    Bitmap.Config.ARGB_8888))).override(post.previewWidth, post.previewHeight)
            .into(imageView)

        binding.favoriteIcon.isVisible = post.favorite

        binding.root.setOnClickListener { _ ->
            onClick(bindingAdapterPosition)
        }
    }
}
