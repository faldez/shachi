package com.faldez.shachi.ui.search_history

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.SearchHistoryListItemBinding
import com.faldez.shachi.model.SearchHistoryServer

class SearchHistoryAdapter(val onClick: (SearchHistoryServer) -> Unit) :
    ListAdapter<SearchHistoryServer, SearchHistoryViewHolder>(POST_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val binding =
            SearchHistoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SearchHistoryViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<SearchHistoryServer>() {
            override fun areItemsTheSame(
                oldItem: SearchHistoryServer,
                newItem: SearchHistoryServer,
            ): Boolean =
                oldItem.searchHistory.searchHistoryId == newItem.searchHistory.searchHistoryId


            override fun areContentsTheSame(
                oldItem: SearchHistoryServer,
                newItem: SearchHistoryServer,
            ): Boolean =
                oldItem == newItem

        }
    }
}

class SearchHistoryViewHolder(
    val binding: SearchHistoryListItemBinding,
    val onClick: (SearchHistoryServer) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(searchHistoryServer: SearchHistoryServer) {
        Log.d("SearchHistoryViewHolder/bind", "item $searchHistoryServer")
        binding.tagsTextview.text = searchHistoryServer.searchHistory.tags
        binding.serverTextview.text = searchHistoryServer.server.title
        binding.root.setOnClickListener {
            onClick(searchHistoryServer)
        }
    }
}