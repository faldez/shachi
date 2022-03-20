package com.faldez.shachi.ui.search

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.R
import com.faldez.shachi.data.model.Category
import com.faldez.shachi.data.model.TagDetail
import com.faldez.shachi.databinding.SearchSuggestionTagListItemBinding

class SearchSuggestionAdapter(
    val setTextColor: (Int) -> ColorStateList,
    val onClick: (TagDetail) -> Unit,
) :
    ListAdapter<TagDetail, SearchSugestionViewHolder>(COMPARATOR) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSugestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = SearchSuggestionTagListItemBinding.inflate(inflater, parent, false)
        return SearchSugestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchSugestionViewHolder, position: Int) {
        val tag = getItem(position)
        val textColor = when (tag.type) {
            Category.General -> R.color.tag_general
            Category.Artist -> R.color.tag_artist
            Category.Copyright -> R.color.tag_copyright
            Category.Character -> R.color.tag_character
            Category.Metadata -> R.color.tag_metadata
            else -> null
        }
        textColor?.let {
            holder.binding.sugestionTagTextView.setTextColor(setTextColor(it))
        }
        holder.binding.sugestionTagTextView.text = "# ${tag.name}"
        holder.binding.root.setOnClickListener {
            onClick(tag)
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<TagDetail>() {
            override fun areItemsTheSame(
                oldItem: TagDetail,
                newItem: TagDetail,
            ): Boolean =
                oldItem.name == newItem.name

            override fun areContentsTheSame(
                oldItem: TagDetail,
                newItem: TagDetail,
            ): Boolean = oldItem == newItem
        }
    }
}

class SearchSugestionViewHolder(val binding: SearchSuggestionTagListItemBinding) :
    RecyclerView.ViewHolder(binding.root)