package com.faldez.shachi.ui.blacklisted_tag

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.BlacklistedTagListItemBinding
import com.faldez.shachi.model.BlacklistedTagWithServer
import com.google.android.material.chip.Chip

class BlacklistedTagAdapter(
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
) : RecyclerView.Adapter<BlacklistedTagItemViewHolder>() {
    private val blacklistedTagList: MutableList<BlacklistedTagWithServer> = mutableListOf()

    fun setData(list: List<BlacklistedTagWithServer>) {
        blacklistedTagList.clear()
        blacklistedTagList.addAll(list)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): BlacklistedTagWithServer {
        return blacklistedTagList[position]
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BlacklistedTagItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlacklistedTagListItemBinding.inflate(inflater, parent, false)

        return BlacklistedTagItemViewHolder(binding, onEdit, onDelete)
    }

    override fun onBindViewHolder(holder: BlacklistedTagItemViewHolder, position: Int) {
        val item = blacklistedTagList[position]
        holder.binding.blacklistedTagsTextview.text =
            item.blacklistedTag.tags

        holder.binding.blacklistedTagsServerTextview.text =
            item.servers.joinToString(", ") { it.title }
    }

    override fun getItemCount(): Int = blacklistedTagList.size
}

class BlacklistedTagItemViewHolder(
    val binding: BlacklistedTagListItemBinding,
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