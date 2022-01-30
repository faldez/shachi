package com.faldez.shachi.ui.saved

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.faldez.shachi.databinding.SavedSearchItemBinding
import com.faldez.shachi.model.SavedSearch

class SavedSearchAdapter(
    private val onBrowse: (SavedSearch) -> Unit,
    private val onDelete: (SavedSearch) -> Unit,
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
        holder.binding.savedSearchTagsTextView.text = item.savedSearch.tags
        holder.binding.savedSearchUrlTextView.text = item.savedSearch.server.url
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        holder.binding.savedSearchItemRecyclerView.layoutManager = layoutManager
        holder.binding.savedSearchItemRecyclerView.adapter =
            SavedSearchItemAdapter(item.posts ?: listOf())

        holder.binding.root.setOnClickListener {
            onBrowse(item.savedSearch)
        }

        holder.binding.deleteSavedSearchButton.setOnClickListener {
            onDelete(item.savedSearch)
        }
    }

    override fun getItemCount(): Int = savedSearches.size
}

class SavedSearchItemViewHolder(val binding: SavedSearchItemBinding) :
    RecyclerView.ViewHolder(binding.root)