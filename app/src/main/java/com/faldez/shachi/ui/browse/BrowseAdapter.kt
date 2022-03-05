package com.faldez.shachi.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.util.MimeUtil
import com.faldez.shachi.util.bindPostImagePreview


class BrowseAdapter(
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
    private val quality: String,
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

        bindPostImagePreview(imageView, post, gridMode, quality, hideQuestionable, hideExplicit)
    }
}
