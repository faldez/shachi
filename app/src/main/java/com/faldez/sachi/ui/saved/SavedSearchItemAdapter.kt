package com.faldez.sachi.ui.saved

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faldez.sachi.databinding.SavedSearchItemPostBinding
import com.faldez.sachi.model.Post

class SavedSearchItemAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<SavedSearchItemPostViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SavedSearchItemPostViewHolder {
        Log.d("SavedSearchItemAdapter", "$posts")
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedSearchItemPostBinding.inflate(inflater, parent, false)
        return SavedSearchItemPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedSearchItemPostViewHolder, position: Int) {
        val item = this.posts.get(position)
        Log.d("SavedSearchItemAdapter", "$item")
        val imageView = holder.binding.previewImage
        Glide.with(imageView.context).load(item.previewUrl)
            .placeholder(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(item.previewWidth!!,
                    item.previewHeight!!,
                    Bitmap.Config.ARGB_8888))).override(item.previewWidth!!, item.previewHeight!!)
            .into(imageView)
    }

    override fun getItemCount(): Int = posts.size
}

class SavedSearchItemPostViewHolder(val binding: SavedSearchItemPostBinding) :
    RecyclerView.ViewHolder(binding.root)