package com.faldez.bonito.ui.servers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.faldez.bonito.databinding.ServerListItemBinding
import com.faldez.bonito.model.Server

class ServerListAdapter : RecyclerView.Adapter<ServerListItemViewHolder>() {
    var serverList: List<Server> = listOf()

    fun setData(list: List<Server>) {
        serverList = list
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
    }

    override fun getItemCount(): Int = serverList.size

}

class ServerListItemViewHolder(val binding: ServerListItemBinding) :
    RecyclerView.ViewHolder(binding.root)