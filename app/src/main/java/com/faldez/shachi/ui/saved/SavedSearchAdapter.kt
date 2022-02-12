package com.faldez.shachi.ui.saved

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.databinding.SavedSearchItemBinding
import com.faldez.shachi.model.SavedSearchServer

class SavedSearchAdapter(
    private val onBrowse: (SavedSearchServer) -> Unit,
    private val onDelete: (SavedSearchServer) -> Unit,
) : ListAdapter<SavedSearchPost, SavedSearchItemViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSearchItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedSearchItemBinding.inflate(inflater, parent, false)

        return SavedSearchItemViewHolder(binding, onBrowse, onDelete)
    }

    override fun onBindViewHolder(holder: SavedSearchItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
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
            ): Boolean =
                oldItem == newItem
        }
    }
}

class SavedSearchItemViewHolder(
    private val binding: SavedSearchItemBinding,
    private val onBrowse: (SavedSearchServer) -> Unit,
    private val onDelete: (SavedSearchServer) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: SavedSearchPost) {
        binding.savedSearchTagsTextView.text = item.savedSearch.savedSearch.savedSearchTitle
        binding.savedSearchServerTextView.text = item.savedSearch.server.title
        binding.tagsTextView.text = SpannableStringBuilder(item.savedSearch.savedSearch.tags)

        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        val adapter = SavedSearchItemAdapter()
        binding.savedSearchItemRecyclerView.apply {
            setLayoutManager(layoutManager)
            setAdapter(adapter)
        }
        adapter.submitList(item.posts ?: listOf())

        binding.root.setOnClickListener {
            onBrowse(item.savedSearch)
        }

        binding.deleteSavedSearchButton.setOnClickListener {
            onDelete(item.savedSearch)
        }
    }
}