package com.faldez.shachi.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.preference.GridMode
import com.faldez.shachi.data.preference.Quality
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.data.util.MimeUtil
import com.faldez.shachi.data.util.bindPostImagePreview


class BrowseAdapter(
    private val gridMode: GridMode,
    private val quality: Quality,
    private val hideQuestionable: Boolean,
    private val hideExplicit: Boolean,
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
                hideQuestionable,
                hideExplicit,
                onClick)
        return binding
    }

    override fun onBindViewHolder(holder: BrowseItemViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    override fun onViewRecycled(holder: BrowseItemViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.binding.root).clear(holder.binding.imageView)
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
    private val gridMode: GridMode,
    private val quality: Quality,
    private val hideQuestionable: Boolean,
    private val hideExplicit: Boolean,
    val onClick: (Int) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post?) {
        val imageView = binding.imageView

        if (post != null) {
            binding.favoriteIcon.isVisible = post.favorite
            binding.movieTypeIcon.isVisible =
                MimeUtil.getMimeTypeFromUrl(post.fileUrl)?.startsWith("video") ?: false
            binding.root.setOnClickListener { _ ->
                onClick(bindingAdapterPosition)
            }
        }

        bindPostImagePreview(binding.root.context,
            imageView,
            post,
            gridMode,
            quality,
            hideQuestionable,
            hideExplicit)
    }
}
