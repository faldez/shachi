package com.faldez.bonito.ui.browse

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faldez.bonito.databinding.PostCardItemBinding
import com.faldez.bonito.model.Post

class BrowserAdapter(private val onClick: (Int) -> Unit) :
    PagingDataAdapter<Post, BrowseItemViewHolder>(POST_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrowseItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = PostCardItemBinding.inflate(inflater, parent, false)
        return BrowseItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrowseItemViewHolder, position: Int) {
        val post = getItem(position)
        post?.let {
            val imageView = holder.binding.imageView
            Glide.with(imageView.context).load(it.previewUrl)
                .placeholder(BitmapDrawable(imageView.resources,
                    Bitmap.createBitmap(it.previewWidth!!,
                        it.previewHeight!!,
                        Bitmap.Config.ARGB_8888))).into(imageView)

            holder.binding.favoriteIcon.visibility = if (post.favorite) {
                View.VISIBLE
            } else {
                View.GONE
            }

            holder.binding.root.setOnClickListener { _ ->
                onClick(position)
            }
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

class BrowseItemViewHolder(val binding: PostCardItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

}
