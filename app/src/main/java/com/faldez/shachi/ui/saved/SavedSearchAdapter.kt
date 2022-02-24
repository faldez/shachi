package com.faldez.shachi.ui.saved

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.forEach
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.paging.filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.RequestOptions
import com.faldez.shachi.R
import com.faldez.shachi.databinding.SavedSearchItemBinding
import com.faldez.shachi.databinding.SavedSearchItemPostBinding
import com.faldez.shachi.model.Post
import com.faldez.shachi.model.Rating
import com.faldez.shachi.model.SavedSearchServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedSearchAdapter(
    private val scrollPositions: SparseIntArray? = null,
    private val onBrowse: ((SavedSearchServer) -> Unit)? = null,
    private val onClick: (SavedSearchServer, Int) -> Unit,
    private val onDelete: ((SavedSearchServer) -> Unit)? = null,
    private val onEdit: ((SavedSearchServer) -> Unit)? = null,
    private val onScroll: ((Int?, Int?) -> Unit)? = null,
    private val savedSearchServer: SavedSearchServer? = null,
    private val gridMode: String,
    private val quality: String,
    private val questionableFilter: String,
    private val explicitFilter: String,
) : PagingDataAdapter<SavedSearchPost, RecyclerView.ViewHolder>(COMPARATOR) {
    private val viewPool = RecyclerView.RecycledViewPool()
    private val adapters: MutableMap<Int, SavedSearchAdapter> = mutableMapOf()

    fun setScrollPositions(newScrollPositions: SparseIntArray) {
        newScrollPositions.forEach { key, value ->
            val oldValue = scrollPositions?.get(key)
            scrollPositions?.put(key, newScrollPositions[key])
            if (oldValue != value) {
                notifyItemChanged(key)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            viewTypeSavedSearch -> {
                val binding = SavedSearchItemBinding.inflate(inflater, parent, false)

                binding.savedSearchItemRecyclerView.layoutManager = if (gridMode == "staggered") {
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
                binding.savedSearchItemRecyclerView.setRecycledViewPool(viewPool)

                SavedSearchItemViewHolder(
                    binding,
                    onBrowse,
                    onDelete,
                    onEdit,
                    onScroll,
                    questionableFilter,
                    explicitFilter,
                )
            }
            viewTypePost -> {
                val binding = SavedSearchItemPostBinding.inflate(inflater, parent, false)
                SavedSearchItemPostViewHolder(
                    binding,
                    onClick,
                    gridMode,
                    quality,
                    hideQuestionable = questionableFilter == "hide",
                    hideExplicit = explicitFilter == "hide",
                )
            }
            else -> {
                throw IllegalAccessException()
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is SavedSearchItemPostViewHolder -> {
                item?.post?.let { holder.bind(it, savedSearchServer!!) }
            }
            is SavedSearchItemViewHolder -> {
                if (item != null) {
                    var adapter = adapters[item.savedSearch!!.savedSearch.savedSearchId]
                    if (adapter == null) {
                        adapter = SavedSearchAdapter(
                            onClick = onClick,
                            onScroll = null,
                            savedSearchServer = item.savedSearch,
                            gridMode = gridMode,
                            quality = quality,
                            questionableFilter = questionableFilter,
                            explicitFilter = explicitFilter,
                        ).apply {
                            stateRestorationPolicy =
                                StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        }

                        adapters[item.savedSearch!!.savedSearch.savedSearchId] = adapter
                    }

                    holder.bind(adapter, scrollPositions!!, item)
                }
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
    private val onBrowse: ((SavedSearchServer) -> Unit)? = null,
    private val onDelete: ((SavedSearchServer) -> Unit)? = null,
    private val onEdit: ((SavedSearchServer) -> Unit)? = null,
    private val onScroll: ((Int?, Int?) -> Unit)? = null,
    private val questionableFilter: String,
    private val explicitFilter: String,
) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(
        adapter: SavedSearchAdapter,
        scrollPositions: SparseIntArray,
        item: SavedSearchPost,
    ) {
        Log.d("SavedSearchItemViewHolder/bind",
            "${item.savedSearch!!.savedSearch.savedSearchId} should scroll to ${scrollPositions[bindingAdapterPosition]}")
        binding.savedSearchTagsTextView.text = item.savedSearch.savedSearch.savedSearchTitle
        binding.savedSearchServerTextView.text = item.savedSearch.server.title
        binding.tagsTextView.text = SpannableStringBuilder(item.savedSearch.savedSearch.tags)

        binding.savedSearchItemRecyclerView.apply {
            setAdapter(adapter)
        }

        binding.savedSearchItemRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val scroll =
                    when (val layoutManager = binding.savedSearchItemRecyclerView.layoutManager) {
                        is StaggeredGridLayoutManager -> layoutManager.findFirstVisibleItemPositions(
                            IntArray(1)).getOrNull(0)
                        is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
                        else -> null
                    }
                onScroll?.let { it(item.savedSearch.savedSearch.savedSearchId, scroll) }
            }
        })

        val scroll = scrollPositions[item.savedSearch.savedSearch.savedSearchId]
        if (scroll > 0) {
            binding.savedSearchItemRecyclerView.post {
                binding.savedSearchItemRecyclerView.layoutManager?.scrollToPosition(scroll)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            item.posts.collectLatest {
                if (it != null) {
                    adapter.submitData(it.filter { item ->
                        when (item.post?.rating) {
                            Rating.Questionable -> questionableFilter != "mute"
                            Rating.Explicit -> explicitFilter != "mute"
                            Rating.Safe -> true
                            else -> true
                        }
                    })
                }
            }
        }

        binding.root.setOnClickListener {
            onBrowse?.let { onBrowse -> onBrowse(item.savedSearch) }
        }
        binding.refreshSavedSearchButton.setOnClickListener {
            adapter.refresh()
        }
        binding.editSavedSearchButton.setOnClickListener {
            onEdit?.let { onEdit -> onEdit(item.savedSearch) }
        }
        binding.deleteSavedSearchButton.setOnClickListener {
            onDelete?.let { onDelete -> onDelete(item.savedSearch) }
        }
    }
}

class SavedSearchItemPostViewHolder(
    val binding: SavedSearchItemPostBinding,
    private val onClick: (SavedSearchServer, Int) -> Unit,
    private val gridMode: String,
    private val quality: String,
    private val hideQuestionable: Boolean,
    private val hideExplicit: Boolean,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Post, savedSearchServer: SavedSearchServer) {
        val imageView = binding.previewImage

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
                glide = glide.apply(RequestOptions().set(Downsampler.ALLOW_HARDWARE_CONFIG, true))
            }
            glide.into(imageView)
        }
        binding.favoriteIcon.isVisible = item.favorite
        binding.root.setOnClickListener {
            Log.d("SavedSearchItemPostViewHolder/bind",
                "${savedSearchServer.savedSearch.savedSearchId} position ${item.postId}")
            onClick(savedSearchServer, bindingAdapterPosition)
        }
    }
}

const val viewTypeSavedSearch = 0
const val viewTypePost = 1
