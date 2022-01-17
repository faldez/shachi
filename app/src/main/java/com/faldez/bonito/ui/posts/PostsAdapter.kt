package com.faldez.bonito.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.faldez.bonito.R
import com.faldez.bonito.databinding.AdapterPostBinding
import com.faldez.bonito.model.Post

class PostsAdapter : PagingDataAdapter<Post, PostsViewHolder>(POST_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val binding = AdapterPostBinding.inflate(inflater, parent, false)
        return PostsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val post = getItem(position)
        Log.d("Adapter", post.toString())
        holder.binding.imageView.load(post?.previewUrl, builder = {
            placeholder(BitmapDrawable(holder.binding.imageView.resources,
                Bitmap.createBitmap(post?.previewWidth!!,
                    post?.previewHeight!!,
                    Bitmap.Config.ARGB_8888)))
        })
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.id == newItem.id


            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem

        }
    }
}

class PostsViewHolder(val binding: AdapterPostBinding) : RecyclerView.ViewHolder(binding.root) {

}
