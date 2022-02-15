package com.faldez.shachi.ui.saved

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.faldez.shachi.databinding.SavedSearchItemBinding
import com.faldez.shachi.databinding.SavedSearchItemPostBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.SavedSearchServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedSearchAdapter(
    private val onBrowse: (SavedSearchServer) -> Unit,
    private val onDelete: (SavedSearchServer) -> Unit,
) : PagingDataAdapter<SavedSearchPost, RecyclerView.ViewHolder>(COMPARATOR) {
    private val viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            viewTypeSavedSearch -> {
                val binding = SavedSearchItemBinding.inflate(inflater, parent, false)
                SavedSearchItemViewHolder(binding, onBrowse, onDelete)
            }
            viewTypePost -> {
                val binding = SavedSearchItemPostBinding.inflate(inflater, parent, false)
                SavedSearchItemPostViewHolder(binding)
            }
            else -> {
                throw IllegalAccessException()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is SavedSearchItemViewHolder -> {
                if (item != null) {
                    holder.bind(viewPool, item)
                }
            }
            is SavedSearchItemPostViewHolder -> {
                item?.post?.let { holder.bind(it) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when {
            item?.savedSearch != null -> {
                viewTypeSavedSearch
            }
            item?.post != null -> {
                viewTypePost
            }
            else -> {
                throw IllegalAccessException()
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<SavedSearchPost>() {
            override fun areItemsTheSame(
                oldItem: SavedSearchPost,
                newItem: SavedSearchPost,
            ): Boolean {
                return if (oldItem.savedSearch != null && newItem.savedSearch != null) {
                    oldItem.savedSearch.savedSearch.savedSearchId == newItem.savedSearch.savedSearch.savedSearchId
                } else if (oldItem.post != null && newItem.post != null) {
                    oldItem.post.postId == newItem.post.postId && oldItem.post.serverId == newItem.post.serverId
                } else {
                    throw IllegalAccessException()
                }
            }


            override fun areContentsTheSame(
                oldItem: SavedSearchPost,
                newItem: SavedSearchPost,
            ): Boolean {
                return if (oldItem.savedSearch != null && newItem.savedSearch != null) {
                    oldItem.savedSearch == newItem.savedSearch
                } else if (oldItem.post != null && newItem.post != null) {
                    oldItem.post == newItem.post
                } else {
                    throw IllegalAccessException()
                }
            }
        }
    }
}

class SavedSearchItemViewHolder(
    private val binding: SavedSearchItemBinding,
    private val onBrowse: (SavedSearchServer) -> Unit,
    private val onDelete: (SavedSearchServer) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(viewPool: RecyclerView.RecycledViewPool, item: SavedSearchPost) {
        binding.savedSearchTagsTextView.text = item.savedSearch?.savedSearch?.savedSearchTitle
        binding.savedSearchServerTextView.text = item.savedSearch?.server?.title
        binding.tagsTextView.text = SpannableStringBuilder(item.savedSearch?.savedSearch?.tags)

        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        val adapter = SavedSearchAdapter(onBrowse, onDelete)
        binding.savedSearchItemRecyclerView.apply {
            setLayoutManager(layoutManager)
            setAdapter(adapter)
            setRecycledViewPool(viewPool)
        }

        CoroutineScope(Dispatchers.IO).launch {
            item.posts.collectLatest {
                if (it != null) {
                    adapter.submitData(it)
                }
            }
        }

        binding.root.setOnClickListener {
            item.savedSearch?.let { it1 -> onBrowse(it1) }
        }

        binding.deleteSavedSearchButton.setOnClickListener {
            item.savedSearch?.let { it1 -> onDelete(it1) }
        }
    }
}

class SavedSearchItemPostViewHolder(val binding: SavedSearchItemPostBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Post) {
        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        val imageView = binding.previewImage
        val previewWidth = item.previewWidth ?: 250
        val previewHeight = item.previewHeight
            ?: (previewWidth * (item.height.toFloat() / item.width.toFloat())).toInt()
        Glide.with(imageView.context).load(item.previewUrl)
            .transition(DrawableTransitionOptions.withCrossFade(factory))
            .placeholder(BitmapDrawable(imageView.resources,
                Bitmap.createBitmap(previewWidth,
                    previewHeight,
                    Bitmap.Config.ARGB_8888))).override(previewWidth, previewHeight)
            .into(imageView)
        binding.root.isChecked = item.favorite
    }
}

const val viewTypeSavedSearch = 0
const val viewTypePost = 1
