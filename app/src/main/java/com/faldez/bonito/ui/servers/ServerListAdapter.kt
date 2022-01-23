package com.faldez.bonito.ui.servers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.faldez.bonito.databinding.ServerListItemBinding
import com.faldez.bonito.model.SelectedServer
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.ServerWithSelected

class ServerListAdapter(
    private val onClick: (Int) -> Unit,
) : RecyclerView.Adapter<ServerListItemViewHolder>() {
    private var serverList: MutableList<ServerWithSelected> = mutableListOf()

    fun setData(list: List<ServerWithSelected>) {
        serverList = list.toMutableList()
        notifyDataSetChanged()
    }

    fun setSelectedServer(serverId: Int?) {
        serverList.find { it.serverId == serverId }?.selected = true
        serverList.find { it.serverId != serverId }?.selected = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ServerListItemBinding.inflate(inflater, parent, false)
        return ServerListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServerListItemViewHolder, position: Int) {
        val server = serverList[position]
        holder.binding.sourceTitleTextview.text = server.title
        holder.binding.sourceUrlTextview.text = server.url
        if (server.selected) {
            holder.binding.sourceSelectedIcon.visibility = View.VISIBLE
        } else {
            holder.binding.sourceSelectedIcon.visibility = View.GONE
        }
        holder.binding.root.setOnClickListener {
            setSelectedServer(serverId = server.serverId)
            onClick(server.serverId)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = serverList.size

}

class ServerListItemViewHolder(val binding: ServerListItemBinding) :
    RecyclerView.ViewHolder(binding.root)