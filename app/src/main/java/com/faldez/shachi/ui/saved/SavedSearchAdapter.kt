package com.faldez.shachi.ui.saved

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.forEach
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.RequestOptions
import com.faldez.shachi.R
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.databinding.SavedSearchItemBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.model.SavedSearchServer
import com.faldez.shachi.util.MimeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedSearchAdapter(
    private val scrollPositions: SparseArray<Int>,
    private val listener: SavedSearchAdapterListener,
    private val gridMode: String,
    private val quality: String,
    private val questionableFilter: String,
    private val explicitFilter: String,
) : ListAdapter<SavedSearchPost, SavedSearchAdapter.SavedSearchViewHolder>(COMPARATOR) {
    private val viewPool = RecyclerView.RecycledViewPool()

    fun setScrollPositions(newScrollPositions: SparseArray<Int>) {
        newScrollPositions.forEach { key, value ->
            val oldValue = scrollPositions.get(key)
            scrollPositions.put(key, newScrollPositions[key])
            if (oldValue != value) {
                notifyItemChanged(key)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSearchViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedSearchItemBinding.inflate(inflater, parent, false)

        binding.savedSearchItemRecyclerView.apply {
            layoutManager = if (gridMode == "staggered") {
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL).apply {
                    gapStrategy =
                        StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
                }
            } else {
                LinearLayoutManager(binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false).apply {
                    recycleChildrenOnDetach = true
                }
            }
            setRecycledViewPool(viewPool)
        }

        return SavedSearchViewHolder(
            binding,
            listener,
            gridMode,
            quality,
            questionableFilter,
            explicitFilter,
        )
    }

    override fun onBindViewHolder(holder: SavedSearchViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item,scrollPositions)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<SavedSearchPost>() {
            override fun areItemsTheSame(
                oldItem: SavedSearchPost,
                newItem: SavedSearchPost,
            ): Boolean =
                oldItem.savedSearch.savedSearch.savedSearchId == newItem.savedSearch.savedSearch.savedSearchId

            override fun areContentsTheSame(
                oldItem: SavedSearchPost,
                newItem: SavedSearchPost,
            ): Boolean = oldItem == newItem
        }
    }

    class SavedSearchViewHolder(
        private val binding: SavedSearchItemBinding,
        private val listener: SavedSearchAdapterListener,
        private val gridMode: String,
        private val quality: String,
        private val questionableFilter: String,
        private val explicitFilter: String,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: SavedSearchPost,
            scrollPositions: SparseArray<Int>,
        ) {
            Log.d("SavedSearchItemViewHolder/bind",
                "${item.savedSearch.savedSearch.savedSearchId} should scroll to ${scrollPositions[bindingAdapterPosition]}")
            binding.savedSearchTagsTextView.text = item.savedSearch.savedSearch.savedSearchTitle
            binding.savedSearchServerTextView.text = item.savedSearch.server.title
            binding.tagsTextView.text = SpannableStringBuilder(item.savedSearch.savedSearch.tags)

            val adapter = SavedSearchPostAdapter(
                listener = listener,
                savedSearchServer = item.savedSearch,
                gridMode = gridMode,
                quality = quality,
                questionableFilter = questionableFilter,
                explicitFilter = explicitFilter,
            ).apply {
                stateRestorationPolicy =
                    StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }

            binding.savedSearchItemRecyclerView.apply {
                swapAdapter(adapter, true)
            }

            scrollPositions[item.savedSearch.savedSearch.savedSearchId]?.let { scroll ->
                if (scroll > 0) {
                    binding.savedSearchItemRecyclerView.post {
                        binding.savedSearchItemRecyclerView.layoutManager?.scrollToPosition(scroll)
                    }
                }
            }

            binding.root.setOnClickListener {
                listener.onBrowse(item.savedSearch)
            }
            binding.refreshSavedSearchButton.setOnClickListener {
                adapter.refresh()
            }
            binding.editSavedSearchButton.setOnClickListener {
                listener.onEdit(item.savedSearch)
            }
            binding.deleteSavedSearchButton.setOnClickListener {
                listener.onDelete(item.savedSearch)
            }

            CoroutineScope(Dispatchers.Main).launch {
                item.posts.collectLatest {
                    if (it != null) {
                        adapter.submitData(it)
                    }
                }
            }
        }
    }
}

class SavedSearchPostAdapter(
    private val listener: SavedSearchAdapterListener,
    private val savedSearchServer: SavedSearchServer? = null,
    private val gridMode: String,
    private val quality: String,
    private val questionableFilter: String,
    private val explicitFilter: String,
) : PagingDataAdapter<Post, SavedSearchPostAdapter.SavedSearchPostViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSearchPostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PostCardItemBinding.inflate(inflater, parent, false)
        return SavedSearchPostViewHolder(
            binding,
            listener,
            gridMode,
            quality,
            hideQuestionable = questionableFilter == "hide",
            hideExplicit = explicitFilter == "hide",
        )
    }

    override fun onBindViewHolder(holder: SavedSearchPostViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, savedSearchServer!!)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean = oldItem.postId == newItem.postId && oldItem.serverId == newItem.serverId

            override fun areContentsTheSame(
                oldItem: Post,
                newItem: Post,
            ): Boolean = oldItem == newItem
        }
    }

    class SavedSearchPostViewHolder(
        val binding: PostCardItemBinding,
        private val listener: SavedSearchAdapterListener,
        private val gridMode: String,
        private val quality: String,
        private val hideQuestionable: Boolean,
        private val hideExplicit: Boolean,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Post?, savedSearchServer: SavedSearchServer) {
            val rootLayoutParams = binding.root.layoutParams
            rootLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            rootLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.root.layoutParams = rootLayoutParams

            val coordinatorLayoutParams = binding.coordinatorLayout.layoutParams
            coordinatorLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            coordinatorLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.coordinatorLayout.layoutParams = coordinatorLayoutParams

            val imageView = binding.imageView
            val imageLayoutParams = imageView.layoutParams
            imageLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            imageLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            imageView.layoutParams = imageLayoutParams

            if (item != null) {
                val previewWidth = item.previewWidth ?: 150
                val previewHeight = if (gridMode == "staggered") {
                    (previewWidth * (item.height.toFloat() / item.width.toFloat())).toInt()
                } else {
                    previewWidth
                }

                if (hideQuestionable && item.rating == Rating.Questionable || hideExplicit && item.rating == Rating.Explicit) {
                    val drawable = ResourcesCompat.getDrawable(binding.root.resources,
                        R.drawable.nsfw_placeholder,
                        null)
                        ?.toBitmap(previewWidth, previewHeight)

                    Glide.with(imageView.context).load(drawable)
                        .into(imageView)
                } else {
                    val url = when (quality) {
                        "sample" -> item.sampleUrl ?: item.previewUrl
                        "original" -> item.fileUrl
                        else -> item.previewUrl ?: item.sampleUrl
                    } ?: item.fileUrl
                    var glide = Glide.with(imageView.context).load(url)
                        .placeholder(BitmapDrawable(imageView.resources,
                            Bitmap.createBitmap(previewWidth,
                                previewHeight,
                                Bitmap.Config.ARGB_8888)))
                        .override(previewWidth, previewHeight)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        glide =
                            glide.apply(RequestOptions().set(Downsampler.ALLOW_HARDWARE_CONFIG,
                                true))
                    }
                    glide.into(imageView)
                }
                binding.favoriteIcon.isVisible = item.favorite
                binding.movieTypeIcon.isVisible =
                    MimeUtil.getMimeTypeFromUrl(item.fileUrl)?.startsWith("video") ?: false
                binding.root.setOnClickListener {
                    listener.onClick(savedSearchServer, bindingAdapterPosition)
                }
            } else {
                val drawable = Bitmap.createBitmap(150,
                    150,
                    Bitmap.Config.ARGB_8888)

                Glide.with(imageView.context).load(drawable)
                    .into(imageView)
            }
        }
    }
}

interface SavedSearchAdapterListener {
    fun onBrowse(savedSearchServer: SavedSearchServer)
    fun onClick(savedSearchServer: SavedSearchServer, position: Int)
    fun onDelete(savedSearchServer: SavedSearchServer)
    fun onEdit(savedSearchServer: SavedSearchServer)
    fun onScroll(position: Int, scroll: Int)
}