package com.faldez.shachi.ui.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.util.MimeUtil
import com.faldez.shachi.util.bindPostImagePreview

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
            holder.binding.movieTypeIcon.isVisible =
                MimeUtil.getMimeTypeFromUrl(post.fileUrl)?.startsWith("video") ?: false
            holder.binding.root.setOnClickListener { _ ->
                onClick(position)
            }
        }

        bindPostImagePreview(imageView, post, gridMode, quality, hideQuestionable, hideExplicit)
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
    RecyclerView.ViewHolder(binding.root)
