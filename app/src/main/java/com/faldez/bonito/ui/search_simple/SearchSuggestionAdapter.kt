package com.faldez.bonito.ui.search_simple

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.faldez.bonito.R
import com.faldez.bonito.databinding.SearchSuggestionTagListItemBinding
import com.faldez.bonito.model.Tag

class SearchSuggestionAdapter(
    val setTextColor: (Int) -> ColorStateList,
    val onClick: (Tag) -> Unit,
) :
    RecyclerView.Adapter<SearchSugestionViewHolder>() {
    var suggestions: MutableList<Tag> = mutableListOf()

    fun clear() {
        suggestions.clear()
    }

    fun setSuggestion(tags: List<Tag>) {
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
            var textColor: Int? = null
            when (tag.type) {
                0 -> {
                    textColor = R.color.tag_general
                }
                1 -> {
                    textColor = R.color.tag_artist
                }
                3 -> {
                    textColor = R.color.tag_copyright
                }
                4 -> {
                    textColor = R.color.tag_character
                }
                5 -> {
                    textColor = R.color.tag_metadata
                }
                else -> null
            }
            textColor?.let {
                holder.binding.sugestionTagTextView.setTextColor(setTextColor(it))
            }
            holder.binding.sugestionTagTextView.text = "# ${tag.name}"
            holder.binding.suggestionTagAddButton.setOnClickListener {
                onClick(tag)
            }
        }
    }

    override fun getItemCount(): Int = suggestions.size
}

class SearchSugestionViewHolder(val binding: SearchSuggestionTagListItemBinding) :
    RecyclerView.ViewHolder(binding.root)