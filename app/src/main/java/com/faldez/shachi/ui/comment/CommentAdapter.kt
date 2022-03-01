package com.faldez.shachi.ui.comment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.databinding.CommentItemBinding
import com.faldez.shachi.model.Comment

class CommentAdapter : ListAdapter<Comment, CommentViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Comment>() {
            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem.id == newItem.id

            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean =
                oldItem == newItem
        }
    }
}

class CommentViewHolder(val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Comment) {
        binding.creatorTextView.text = item.creator
        binding.dateTextView.text = item.createdAt?.toString()
        binding.bodyTextView.text = item.body
    }
}