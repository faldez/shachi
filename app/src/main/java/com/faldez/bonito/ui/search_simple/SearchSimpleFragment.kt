package com.faldez.bonito.ui.search_simple

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faldez.bonito.MainActivity
import com.faldez.bonito.R
import com.faldez.bonito.data.TagRepository
import com.faldez.bonito.databinding.SearchSimpleFragmentBinding
import com.faldez.bonito.model.Server
import com.faldez.bonito.model.Tag
import com.faldez.bonito.service.BooruService
import com.google.android.material.chip.Chip
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class SearchSimpleFragment : Fragment() {
    private lateinit var binding: SearchSimpleFragmentBinding
    private lateinit var viewModel: SearchSimpleViewModel
    private lateinit var searchSuggestionAdapter: SearchSuggestionAdapter
    private lateinit var searchSimpleMenu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = SearchSimpleFragmentBinding.inflate(inflater, container, false)

        val initialTags: List<Tag>? = requireArguments().get("tags") as List<Tag>?
        val server = requireArguments().getParcelable<Server?>("server")
        viewModel =
            SearchSimpleViewModel(server, initialTags ?: listOf(), TagRepository(BooruService()))

        binding.searchSimpleTagsInputText.bind()
        prepareAppBar()

        binding.suggestionTagsRecyclerView.bind()


        return binding.root
    }

    private fun RecyclerView.bind() {
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(this.context,
            linearLayoutManager.orientation)
        this.addItemDecoration(dividerItemDecoration)
        searchSuggestionAdapter = SearchSuggestionAdapter(
            setTextColor = {
                ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                    it,
                    requireActivity().theme))

            },
            onClick = {
                viewModel.insertTag(it)
                binding.searchSimpleTagsInputText.text?.clear()
            }
        )
        this.apply {
            adapter = searchSuggestionAdapter
            layoutManager = linearLayoutManager

        }
    }

    private fun prepareAppBar() {
        (activity as MainActivity).setSupportActionBar(binding.searchSimpleTopAppBar)
        binding.searchSimpleAppBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        val supportActionBar = (activity as MainActivity).supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                (activity as MainActivity).onBackPressed()
                return true
            }
            R.id.clear_button -> {
                binding.searchSimpleTagsInputText.text?.clear()
                return true
            }
            R.id.apply_button -> {
                findNavController().previousBackStackEntry?.savedStateHandle?.set("tags",
                    viewModel.selectedTags.value)
                (activity as MainActivity).onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_simple_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        searchSimpleMenu = menu
    }

    private fun NestedScrollView.show() {
        val view = this
        view.animate().alpha(1f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.VISIBLE
                }
            })
    }

    private fun NestedScrollView.hide() {
        val view = this
        view.animate().alpha(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.GONE
                }
            })
    }


    private fun TextInputEditText.bind() {
        this.apply {
            setImeActionLabel("Add", KeyEvent.KEYCODE_ENTER)
            doOnTextChanged { text, _, _, _ ->
                if (text.isNullOrEmpty()) {
                    searchSuggestionAdapter.clear()
                    binding.selectedTagsLayout.show()
                    binding.suggestionTagLayout.hide()
                    searchSimpleMenu.getItem(0).isVisible = false
                } else {
                    viewModel.accept(UiAction.SearchTag(viewModel.server, text.toString()))
                    binding.selectedTagsLayout.hide()
                    binding.suggestionTagLayout.show()
                    searchSimpleMenu.getItem(0).isVisible = true
                }
            }
            setOnEditorActionListener { textView, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    val text = (textView as TextInputEditText).text.toString().removePrefix("# ")
                    if (text.isNotEmpty()) {
                        viewModel.insertTagByName(text)
                        binding.searchSimpleTagsInputText.text?.clear()
                    }
                    true
                } else {
                    false
                }
            }
        }

        lifecycleScope.launch {
            viewModel.tags.collect {
                if (it.isNullOrEmpty()) {
                    binding.suggestionTagsHeader.visibility = View.GONE
                } else {
                    binding.suggestionTagsHeader.visibility = View.VISIBLE
                }
                it?.let { tags ->
                    searchSuggestionAdapter.setSuggestion(tags)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedTags.collect { tags ->
                val groupedTags = tags.groupBy { it.type }
                Log.d("SearchSimpleFragment", "collect $groupedTags")
                binding.selectedTagsHeader.isVisible = groupedTags.isNotEmpty()
                binding.generalTagsHeader.isVisible = false
                binding.artistTagsHeader.isVisible = false
                binding.copyrightTagsHeader.isVisible = false
                binding.characterTagsHeader.isVisible = false
                binding.metadataTagsHeader.isVisible = false
                binding.otherTagsHeader.isVisible = false

                groupedTags.forEach { (type, tags) ->
                    var group = binding.otherTagsChipGroup
                    var header = binding.otherTagsHeader
                    when (type) {
                        0 -> {
                            group = binding.generalTagsChipGroup
                            header = binding.generalTagsHeader
                        }
                        1 -> {
                            group = binding.artistTagsChipGroup
                            header = binding.artistTagsHeader
                        }
                        3 -> {
                            group = binding.copyrightTagsChipGroup
                            header = binding.copyrightTagsHeader
                        }
                        4 -> {
                            group = binding.characterTagsChipGroup
                            header = binding.characterTagsHeader
                        }
                        5 -> {
                            group = binding.metadataTagsChipGroup
                            header = binding.metadataTagsHeader
                        }
                        else -> binding.otherTagsChipGroup
                    }
                    group.removeAllViews()
                    header.isVisible = tags.isNotEmpty()
                    group.isVisible = tags.isNotEmpty()

                    tags.forEach { tag ->
                        val chip = Chip(requireContext())
                        chip.bind(tag)
                    }
                }

            }
        }
    }

    private fun Chip.bind(tag: Tag) {
        var textColor: Int? = null
        var group = binding.otherTagsChipGroup
        when (tag.type) {
            0 -> {
                textColor = R.color.tag_general
                group = binding.generalTagsChipGroup
            }
            1 -> {
                textColor = R.color.tag_artist
                group = binding.artistTagsChipGroup
            }
            3 -> {
                textColor = R.color.tag_copyright
                group = binding.copyrightTagsChipGroup
            }
            4 -> {
                textColor = R.color.tag_character
                group = binding.characterTagsChipGroup
            }
            5 -> {
                textColor = R.color.tag_metadata
                group = binding.metadataTagsChipGroup
            }
        }

        this.apply {
            text = tag.name
            isCloseIconVisible = true
            textColor?.let {
                setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                    textColor,
                    requireActivity().theme)))
            }
            setOnCloseIconClickListener {
                Log.d("SearchSimpleViewModel", "onClose")
                viewModel.removeTag(tag)
                group.removeView(it)
            }
            setOnClickListener {

            }

            group.addView(this)
        }
    }
}