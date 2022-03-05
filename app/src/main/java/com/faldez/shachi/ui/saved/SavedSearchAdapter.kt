package com.faldez.shachi.ui.saved

import android.text.SpannableStringBuilder
import android.util.Log
import android.util.SparseArray
import android.view.*
import androidx.core.util.forEach
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.paging.filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.PostCardItemBinding
import com.faldez.shachi.databinding.SavedSearchItemBinding
import com.faldez.shachi.data.model.Post
import com.faldez.shachi.data.model.Rating
import com.faldez.shachi.data.model.SavedSearchServer
import com.faldez.shachi.util.MimeUtil
import com.faldez.shachi.util.bindPostImagePreview
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
    val adapters: MutableMap<Int, SavedSearchPostAdapter> = mutableMapOf()

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
            layoutManager = LinearLayoutManager(binding.root.context,
                LinearLayoutManager.HORIZONTAL,
                false).apply {
                recycleChildrenOnDetach = true
                initialPrefetchItemCount = 10
            }
            setRecycledViewPool(viewPool)
            setHasFixedSize(true)
        }

        return SavedSearchViewHolder(
            binding,
            listener,
        )
    }

    override fun onBindViewHolder(holder: SavedSearchViewHolder, position: Int) {
        val item = getItem(position)

        val adapter = SavedSearchPostAdapter(
            listener = listener,
            savedSearchServer = item.savedSearch,
            gridMode = gridMode,
            quality = quality,
            questionableFilter = questionableFilter,
            explicitFilter = explicitFilter,
        )

        adapters[position] = adapter

        CoroutineScope(Dispatchers.IO).launch {
            item.posts.collectLatest {
                if (it != null) {
                    adapter.submitData(it.filter { item ->
                        when (item.rating) {
                            Rating.Questionable -> questionableFilter != "mute"
                            Rating.Explicit -> explicitFilter != "mute"
                            Rating.Safe -> true
                        }
                    })
                }
            }
        }

        holder.bind(adapter, item, scrollPositions)
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
            ): Boolean = oldItem.savedSearch == newItem.savedSearch
        }
    }

    class SavedSearchViewHolder(
        val binding: SavedSearchItemBinding,
        private val listener: SavedSearchAdapterListener,
    ) :
        RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {

        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(
            adapter: SavedSearchPostAdapter,
            item: SavedSearchPost,
            scrollPositions: SparseArray<Int>,
        ) {
            Log.d("SavedSearchItemViewHolder/bind",
                "${item.savedSearch.savedSearch.savedSearchId} should scroll to ${scrollPositions[bindingAdapterPosition]}")
            binding.savedSearchTagsTextView.text = item.savedSearch.savedSearch.savedSearchTitle
            binding.savedSearchServerTextView.text = item.savedSearch.server.title
            binding.tagsTextView.text = SpannableStringBuilder(item.savedSearch.savedSearch.tags)

            binding.savedSearchItemRecyclerView.apply {
                swapAdapter(adapter, true)
            }

            binding.root.setOnClickListener {
                listener.onBrowse(item.savedSearch)
            }

            scrollPositions[item.savedSearch.savedSearch.savedSearchId]?.let { scroll ->
                if (scroll > 0) {
                    binding.savedSearchItemRecyclerView.post {
                        binding.savedSearchItemRecyclerView.layoutManager?.scrollToPosition(
                            scroll)
                    }
                }
            }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?,
        ) {
            val refresh = menu?.add(Menu.NONE, 1, 1, "Refresh")
            val edit = menu?.add(Menu.NONE, 2, 2, "Edit")
            val delete = menu?.add(Menu.NONE, 3, 3, "Delete")

            refresh?.setOnMenuItemClickListener {
                listener.onRefresh(bindingAdapterPosition)
                true
            }

            edit?.setOnMenuItemClickListener {
                listener.onEdit(bindingAdapterPosition)
                true
            }
            delete?.setOnMenuItemClickListener {
                listener.onDelete(bindingAdapterPosition)
                true
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
                if (item.favorite)
                    binding.favoriteIcon.isVisible = true
                else
                    binding.favoriteIcon.isGone = true
                if (MimeUtil.getMimeTypeFromUrl(item.fileUrl)?.startsWith("video") == true)
                    binding.movieTypeIcon.isVisible = true
                else
                    binding.movieTypeIcon.isGone = true
                binding.root.setOnClickListener {
                    listener.onClick(savedSearchServer, bindingAdapterPosition)
                }
            }

            bindPostImagePreview(imageView, item, gridMode, quality, hideQuestionable, hideExplicit)
        }
    }
}

interface SavedSearchAdapterListener {
    fun onBrowse(savedSearchServer: SavedSearchServer)
    fun onClick(savedSearchServer: SavedSearchServer, position: Int)
    fun onRefresh(position: Int)
    fun onDelete(position: Int)
    fun onEdit(position: Int)
    fun onScroll(position: Int, scroll: Int)
}