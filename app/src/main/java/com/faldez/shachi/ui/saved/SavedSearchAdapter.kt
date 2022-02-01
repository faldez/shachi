package com.faldez.shachi.ui.saved

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.databinding.SavedSearchItemBinding
import com.faldez.shachi.model.SavedSearch
import com.faldez.shachi.model.SavedSearchServer
import com.google.android.material.chip.Chip

class SavedSearchAdapter(
    private val onBrowse: (SavedSearchServer) -> Unit,
    private val onDelete: (SavedSearchServer) -> Unit,
) :
    RecyclerView.Adapter<SavedSearchItemViewHolder>() {
    private val savedSearches: MutableList<SavedSearchPost> = mutableListOf()

    fun updateDate(savedSearch: List<SavedSearchPost>) {
        savedSearch.forEach {

        }
        savedSearches.clear()
        savedSearches.addAll(savedSearch)
        notifyDataSetChanged()
    }

    fun updatePosts(savedSearchPost: SavedSearchPost) {
        val index = savedSearches.indexOfFirst {
            it.savedSearch == savedSearchPost.savedSearch
        }

        if (index > -1) {
            val item = savedSearches.get(index)
            if (item.posts != null && savedSearchPost.posts == null) {
                return
            }
            savedSearches[index] = item.copy(posts = savedSearchPost.posts)
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSearchItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedSearchItemBinding.inflate(inflater, parent, false)

        return SavedSearchItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedSearchItemViewHolder, position: Int) {
        val item = savedSearches[position]
        Log.d("SavedSearchAdapter", "$item")
        holder.binding.savedSearchTagsTextView.text = item.savedSearch.savedSearch.savedSearchTitle
        holder.binding.savedSearchUrlTextView.text = item.savedSearch.server.url
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        holder.binding.savedSearchItemRecyclerView.layoutManager = layoutManager
        holder.binding.savedSearchItemRecyclerView.adapter =
            SavedSearchItemAdapter(item.posts ?: listOf())

        holder.binding.root.setOnClickListener {
            onBrowse(item.savedSearch)
        }

        holder.binding.tagsChipGroup.removeAllViews()
        item.savedSearch.savedSearch.tags.split(" ").forEach {
            val chip = Chip(holder.binding.root.context)
            chip.apply {
                text = it
                holder.binding.tagsChipGroup.addView(this)
            }
        }

        holder.binding.removeSavedSearchButton.setOnClickListener {
            onDelete(item.savedSearch)
        }
    }

    override fun getItemCount(): Int = savedSearches.size
}

class SavedSearchItemViewHolder(val binding: SavedSearchItemBinding) :
    RecyclerView.ViewHolder(binding.root)