package com.faldez.shachi.ui.server_dialog

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.ServerListDialogItemBinding
import com.faldez.shachi.data.model.ServerView

class ServerDialogAdapter :
    ListAdapter<ServerView, ServerDialogViewHolder>(POST_COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerDialogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ServerListDialogItemBinding.inflate(inflater, parent, false)

        return ServerDialogViewHolder(binding)
    }

    private fun setSelected(serverId: Int) {
        currentList.toMutableList().forEachIndexed { index, server ->
            val oldState = server.selected
            val newState = server.serverId == serverId
            server.selected = server.serverId == serverId

            if (oldState != newState) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onBindViewHolder(holder: ServerDialogViewHolder, position: Int) {
        val server = getItem(position)
        holder.binding.sourceTitleTextview.text = server.title
        holder.binding.sourceUrlTextview.text = server.url
        holder.binding.root.isChecked = server.selected
        holder.binding.root.setOnClickListener {
            setSelected(serverId = server.serverId)
            Log.d("ServerDialogAdapter", "select ${server.serverId}")
        }
    }

    companion object {
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<ServerView>() {
            override fun areItemsTheSame(oldItem: ServerView, newItem: ServerView): Boolean =
                oldItem.serverId == newItem.serverId


            override fun areContentsTheSame(oldItem: ServerView, newItem: ServerView): Boolean =
                oldItem == newItem

        }
    }

}

class ServerDialogViewHolder(
    val binding: ServerListDialogItemBinding,
) :
    RecyclerView.ViewHolder(binding.root)