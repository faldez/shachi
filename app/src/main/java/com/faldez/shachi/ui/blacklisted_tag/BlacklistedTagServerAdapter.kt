package com.faldez.shachi.ui.blacklisted_tag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.BlacklistedTagEditServerItemBinding
import com.faldez.shachi.model.Server
import com.faldez.shachi.model.ServerView

class BlacklistedTagServerAdapter(
    private val servers: List<ServerView>,
    private val selectedServer: MutableList<Boolean>,
) :
    RecyclerView.Adapter<BlacklistedTagServerItemViewHolder>() {
    fun getSelected(): List<Server> {
        return servers.filterIndexed { index, serverView ->
            selectedServer[index]
        }.map {
            Server(
                serverId = it.serverId,
                type = it.type,
                title = it.title,
                url = it.url
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BlacklistedTagServerItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlacklistedTagEditServerItemBinding.inflate(inflater, parent, false)

        return BlacklistedTagServerItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlacklistedTagServerItemViewHolder, position: Int) {
        val server = servers[position]
        holder.binding.blacklistedTagsServerName.text = server.title
        holder.binding.blacklistedTagsServerUrl.text = server.url
        holder.binding.blacklistedTagsServerCheckbox.isChecked = selectedServer[position]

        holder.binding.root.setOnClickListener {
            selectedServer[position] = !selectedServer[position]
            holder.binding.blacklistedTagsServerCheckbox.isChecked = selectedServer[position]
        }

        holder.binding.blacklistedTagsServerCheckbox.setOnCheckedChangeListener { _, b ->
            selectedServer[position] = b
        }
    }

    override fun getItemCount(): Int = servers.size
}

class BlacklistedTagServerItemViewHolder(val binding: BlacklistedTagEditServerItemBinding) :
    RecyclerView.ViewHolder(binding.root) {}