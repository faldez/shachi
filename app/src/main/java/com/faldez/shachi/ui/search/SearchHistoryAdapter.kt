package com.faldez.shachi.ui.search

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.SearchHistoryListItemBinding
import com.faldez.shachi.data.model.SearchHistoryServer

class SearchHistoryAdapter(
    val onClick: (SearchHistoryServer) -> Unit,
    val onDelete: (SearchHistoryServer) -> Unit,
) :
    PagingDataAdapter<SearchHistoryServer, SearchHistoryViewHolder>(POST_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val binding =
            SearchHistoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return SearchHistoryViewHolder(binding, onClick, onDelete)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
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
    val onDelete: (SearchHistoryServer) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(searchHistoryServer: SearchHistoryServer) {
        Log.d("SearchHistoryViewHolder/bind", "item $searchHistoryServer")
        binding.tagsTextview.text = searchHistoryServer.searchHistory.tags
        binding.serverTextview.text = searchHistoryServer.server.title
        binding.historyLayout.setOnClickListener {
            onClick(searchHistoryServer)
        }
        binding.deleteHistoryButton.setOnClickListener {
            onDelete(searchHistoryServer)
        }
    }
}