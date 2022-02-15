package com.faldez.shachi.ui.search

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.R
import com.faldez.shachi.databinding.SearchSuggestionTagListItemBinding
import com.faldez.shachi.model.Category
import com.faldez.shachi.model.TagDetail

class SearchSuggestionAdapter(
    val setTextColor: (Int) -> ColorStateList,
    val onClick: (TagDetail) -> Unit,
) :
    RecyclerView.Adapter<SearchSugestionViewHolder>() {
    var suggestions: MutableList<TagDetail> = mutableListOf()

    fun clear() {
        suggestions.clear()
    }

    fun setSuggestion(tags: List<TagDetail>) {
        suggestions.clear()
        suggestions.addAll(tags)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSugestionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = SearchSuggestionTagListItemBinding.inflate(inflater, parent, false)
        return SearchSugestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchSugestionViewHolder, position: Int) {
        suggestions[position].let { tag ->
            val textColor  = when (tag.type) {
                Category.General -> R.color.tag_general
                Category.Artist -> R.color.tag_artist
                Category.Copyright -> R.color.tag_copyright
                Category.Character -> R.color.tag_character
                Category.Metadata -> R.color.tag_metadata
            }
            textColor?.let {
                holder.binding.sugestionTagTextView.setTextColor(setTextColor(it))
            }
            holder.binding.sugestionTagTextView.text = "# ${tag.name}"
            holder.binding.root.setOnClickListener {
                onClick(tag)
            }
        }
    }

    override fun getItemCount(): Int = suggestions.size
}

class SearchSugestionViewHolder(val binding: SearchSuggestionTagListItemBinding) :
    RecyclerView.ViewHolder(binding.root)