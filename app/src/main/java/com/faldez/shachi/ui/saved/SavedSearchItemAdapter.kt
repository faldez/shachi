package com.faldez.shachi.ui.saved

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.faldez.shachi.databinding.SavedSearchItemPostBinding
import com.faldez.shachi.model.Post

class SavedSearchItemAdapter :
    ListAdapter<Post, SavedSearchItemPostViewHolder>(COMPARATOR) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SavedSearchItemPostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedSearchItemPostBinding.inflate(inflater, parent, false)
        return SavedSearchItemPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedSearchItemPostViewHolder, position: Int) {
        val item = getItem(position)
        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        val imageView = holder.binding.previewImage
        val previewWidth = item.previewWidth ?: 250
        val previewHeight = item.previewHeight ?: (previewWidth * (item.height.toFloat() / item.width.toFloat())).toInt()
        Glide.with(imageView.context).load(item.previewUrl)
            .transition(withCrossFade(factory))
            .placeholder(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(previewWidth,
                    previewHeight,
                    Bitmap.Config.ARGB_8888))).override(previewWidth, previewHeight)
            .into(imageView)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean =
                oldItem.postId == newItem.postId && oldItem.serverId == newItem.serverId


            override fun areContentsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean =
                oldItem == newItem

        }
    }
}

class SavedSearchItemPostViewHolder(val binding: SavedSearchItemPostBinding) :
    RecyclerView.ViewHolder(binding.root)