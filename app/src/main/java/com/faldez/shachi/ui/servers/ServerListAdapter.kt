package com.faldez.shachi.ui.servers

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.ServerListItemBinding
import com.faldez.shachi.model.ServerWithSelected

class ServerListAdapter(
    private val onTap: (Int) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<ServerListItemViewHolder>() {
    var serverList: MutableList<ServerWithSelected> = mutableListOf()

    fun setData(list: List<ServerWithSelected>) {
        serverList = list.toMutableList()
        notifyDataSetChanged()
    }

    fun setSelectedServer(serverUrl: String?) {
        serverList.find { it.url == serverUrl }?.selected = true
        serverList.find { it.url != serverUrl }?.selected = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ServerListItemBinding.inflate(inflater, parent, false)
        return ServerListItemViewHolder(binding, onEdit, onDelete)
    }


    override fun onBindViewHolder(holder: ServerListItemViewHolder, position: Int) {
        val server = serverList[position]
        val view = holder.binding
        view.sourceTitleTextview.text = server.title
        view.sourceUrlTextview.text = server.url
        view.root.isChecked = server.selected
        view.root.setOnClickListener {
            setSelectedServer(serverUrl = server.url)
            onTap(server.serverId)
            notifyItemChanged(position)
        }

//        view.serverEditButton.setOnClickListener {
//            onEdit(server)
//        }
//        view.serverDeleteButton.setOnClickListener {
//            onDelete(server)
//        }

    }

    override fun getItemCount(): Int = serverList.size
}

class ServerListItemViewHolder(
    val binding: ServerListItemBinding,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
) :
    RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
    init {
        binding.root.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        view: View?,
        contextMenu: ContextMenu.ContextMenuInfo?,
    ) {
        val edit = menu?.add(Menu.NONE, 1, 1, "Edit")
        val delete = menu?.add(Menu.NONE, 2, 2, "Delete")

        edit?.setOnMenuItemClickListener {
            onMenuItemClick(it)
        }
        delete?.setOnMenuItemClickListener {
            onMenuItemClick(it)
        }
    }

    private fun onMenuItemClick(it: MenuItem): Boolean {
        when (it.itemId) {
            1 -> {
                onEdit(bindingAdapterPosition)
                return true
            }
            2 -> {
                onDelete(bindingAdapterPosition)
                return true
            }
        }
        return false
    }

}