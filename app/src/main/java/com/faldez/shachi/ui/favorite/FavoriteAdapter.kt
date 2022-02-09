package com.faldez.shachi.ui.favorite

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.model.Post

class FavoriteAdapter(
    private val gridMode: String, private val quality: String, private val onClick: (Int) -> Unit,
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

        post?.let {
            val imageView = holder.binding.imageView

            val previewWidth = it.previewWidth ?: 150
            val previewHeight = if (gridMode == "staggered") {
                it.previewHeight
                    ?: (previewWidth * (it.height.toFloat() / it.width.toFloat())).toInt()
            } else {
                previewWidth
            }

            val url = when (quality) {
                "sample" -> it.sampleUrl ?: it.previewUrl
                "original" -> it.fileUrl
                else -> it.previewUrl ?: it.sampleUrl
            } ?: it.fileUrl

            Glide.with(imageView.context).load(url)
                .placeholder(BitmapDrawable(imageView.resources,
                    Bitmap.createBitmap(previewWidth,
                        previewHeight,
                        Bitmap.Config.ARGB_8888))).override(previewWidth, previewHeight)
                .into(imageView)

            holder.binding.root.setOnClickListener { _ ->
                onClick(position)
            }
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
