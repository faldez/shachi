package com.faldez.shachi.ui.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ScrollView
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.MainActivity
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.SearchFragmentBinding
import com.faldez.shachi.databinding.TagsDetailsBinding
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.TagDetail
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.BooruService
import com.faldez.shachi.util.StringUtil
import com.google.android.material.chip.Chip
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {
    private lateinit var binding: SearchFragmentBinding
    private lateinit var tagDetailsBinding: TagsDetailsBinding

    private val viewModel: SearchSimpleViewModel by viewModels {
        val server: ServerView =
            requireArguments().getParcelable<ServerView>("server") as ServerView
        SearchViewModelFactory(server, TagRepository(BooruService(),
            AppDatabase.build(requireContext())), this)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

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
        binding = SearchFragmentBinding.inflate(inflater, container, false)
        tagDetailsBinding = TagsDetailsBinding.bind(binding.root)

        binding.searchSimpleTagsInputText.bind()
        prepareAppBar()

        binding.suggestionTagsRecyclerView.bind()

        val initialTags: String = requireArguments().get("tags") as String

        bindSelectedTags(initialTags)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isAdvancedMode) {
                    binding.selectedTagsLayout.hide()
                    binding.suggestionTagLayout.show()
                }
            }
        }

        return binding.root
    }

    private fun bindSelectedTags(initialTags: String) {
        binding.loadingIndicator.isVisible = initialTags.isNotEmpty()

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                val tags = state.selectedTags
                if (tags is SelectedTags.Simple) {
                    val groupedTags = tags.tags.groupBy { it.type }
                    Log.d("SearchSimpleFragment", "collect $groupedTags")
                    binding.selectedTagsHeader.isVisible = groupedTags.isNotEmpty()
                    tagDetailsBinding.generalTagsHeader.isVisible = false
                    tagDetailsBinding.artistTagsHeader.isVisible = false
                    tagDetailsBinding.copyrightTagsHeader.isVisible = false
                    tagDetailsBinding.characterTagsHeader.isVisible = false
                    tagDetailsBinding.metadataTagsHeader.isVisible = false
                    tagDetailsBinding.otherTagsHeader.isVisible = false

                    groupedTags.forEach { (type, tags) ->
                        var group = tagDetailsBinding.otherTagsChipGroup
                        var header = tagDetailsBinding.otherTagsHeader
                        when (type) {
                            0 -> {
                                group = tagDetailsBinding.generalTagsChipGroup
                                header = tagDetailsBinding.generalTagsHeader
                            }
                            1 -> {
                                group = tagDetailsBinding.artistTagsChipGroup
                                header = tagDetailsBinding.artistTagsHeader
                            }
                            3 -> {
                                group = tagDetailsBinding.copyrightTagsChipGroup
                                header = tagDetailsBinding.copyrightTagsHeader
                            }
                            4 -> {
                                group = tagDetailsBinding.characterTagsChipGroup
                                header = tagDetailsBinding.characterTagsHeader
                            }
                            5 -> {
                                group = tagDetailsBinding.metadataTagsChipGroup
                                header = tagDetailsBinding.metadataTagsHeader
                            }
                            else -> tagDetailsBinding.otherTagsChipGroup
                        }
                        group.removeAllViews()
                        header.isVisible = tags.isNotEmpty()
                        group.isVisible = tags.isNotEmpty()

                        tags.forEach { tag ->
                            val chip = Chip(requireContext())
                            chip.bind(tag)
                        }
                    }
                    binding.loadingIndicator.isVisible = false
                }
            }
        }
        viewModel.setInitialTags(initialTags, getSearchMode())
        if (getSearchMode()) {
            binding.searchSimpleTagsInputText.text = SpannableStringBuilder(initialTags)
        }
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
                if (viewModel.state.value.isAdvancedMode) {
                    val text = binding.searchSimpleTagsInputText.text?.toString()
                    if (text.isNullOrEmpty()) {
                        binding.searchSimpleTagsInputText.text = SpannableStringBuilder(it.name)
                        binding.searchSimpleTagsInputText.setSelection(it.name.length)
                    } else {
                        val selectionStart = binding.searchSimpleTagsInputText.selectionStart - 1
                        val start =
                            StringUtil.findTokenStart(text, selectionStart)
                        val end = StringUtil.findTokenEnd(text, selectionStart)
                        Log.d("SearchFragment",
                            "selectionStart=$selectionStart text=$text replace start=$start to end=$end with=${it.name}")
                        binding.searchSimpleTagsInputText.text?.replace(start, end + 1, it.name)
                        binding.searchSimpleTagsInputText.setSelection(start + it.name.length)
                    }
                } else {
                    viewModel.insertTag(it)
                    binding.searchSimpleTagsInputText.text?.clear()
                }
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
                applySearch()
                return true
            }
            R.id.search_mode_button -> {
                viewModel.setMode(!viewModel.state.value.isAdvancedMode)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun applySearch() {
        val value = when (val selectedTags = viewModel.state.value.selectedTags) {
            is SelectedTags.Simple -> {
                selectedTags.tags.joinToString(" ") { it.toString() }
            }
            is SelectedTags.Advance -> {
                selectedTags.tags
            }
            else -> {
                ""
            }
        }
        findNavController().previousBackStackEntry?.savedStateHandle?.set("tags", Pair(null, value))
        (activity as MainActivity).onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        searchSimpleMenu = menu
        searchSimpleMenu.bind()
    }

    private fun Menu.bind() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                getItem(2).isChecked = state.isAdvancedMode
                saveSearchMode(mode = state.isAdvancedMode)
            }
        }
    }

    private fun getSearchMode(): Boolean = sharedPreferences.getBoolean("search_mode", false)
    private fun saveSearchMode(mode: Boolean) = sharedPreferences.edit {
        putBoolean("search_mode", mode)
        commit()
    }

    private fun ScrollView.show() {
        val view = this
        view.animate().alpha(1f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    view.visibility = View.VISIBLE
                }
            })
    }

    private fun ScrollView.hide() {
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
            doAfterTextChanged { s ->
                if (viewModel.state.value.isAdvancedMode) {
                    s?.toString()?.let {
                        viewModel.state.value =
                            viewModel.state.value.copy(selectedTags = SelectedTags.Advance(it))
                    }
                }
            }
            doOnTextChanged { text, start, before, count ->
                if (viewModel.state.value.isAdvancedMode) {
                    Log.d("SearchFragment", "text=$text start=$start before=$before count=$count")
                    text?.toString()?.trim()?.let {
                        Log.d("SearchFragment", "text.isNotEmpty=${it.isNotEmpty()}")
                        if (it.isNotEmpty()) {
                            val tag = StringUtil.getCurrentToken(it, start)
                            viewModel.accept(UiAction.SearchTag(tag))
                        }
                    }
                } else {
                    if (text.isNullOrEmpty()) {
                        searchSuggestionAdapter.clear()
                        binding.selectedTagsLayout.show()
                        binding.suggestionTagLayout.hide()
                        binding.loadingIndicator.isVisible = false
                        searchSimpleMenu.getItem(0).isVisible = false
                    } else {
                        viewModel.accept(UiAction.SearchTag(text.toString()))
                        binding.selectedTagsLayout.hide()
                        binding.suggestionTagLayout.show()
                        binding.loadingIndicator.isVisible = true
                        searchSimpleMenu.getItem(0).isVisible = true
                    }
                }
            }
            setOnEditorActionListener { textView, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    /*
                    If advanced search is active, action done will apply search
                    instead insert tag into selected tags or apply search if empty
                    on simple mode
                     */
                    if (viewModel.state.value.isAdvancedMode) {
                        applySearch()
                    } else {
                        val text = (textView as TextInputEditText).text.toString()
                        if (text.isNotEmpty()) {
                            viewModel.insertTagByName(text)
                            binding.searchSimpleTagsInputText.text?.clear()
                        } else if (text.isEmpty() && viewModel.state.value.selectedTags?.isNotEmpty() == true) {
                            applySearch()
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }

        lifecycleScope.launch {
            viewModel.suggestionTags.collect {
                if (it.isNullOrEmpty()) {
                    binding.suggestionTagsHeader.visibility = View.GONE
                } else {
                    binding.suggestionTagsHeader.visibility = View.VISIBLE
                }
                it?.let { tags ->
                    searchSuggestionAdapter.setSuggestion(tags)
                }

                binding.loadingIndicator.isVisible = false
            }
        }
    }

    private fun Chip.bind(tag: TagDetail) {
        var textColor: Int? = null
        var group = tagDetailsBinding.otherTagsChipGroup
        when (tag.type) {
            0 -> {
                textColor = R.color.tag_general
                group = tagDetailsBinding.generalTagsChipGroup
            }
            1 -> {
                textColor = R.color.tag_artist
                group = tagDetailsBinding.artistTagsChipGroup
            }
            3 -> {
                textColor = R.color.tag_copyright
                group = tagDetailsBinding.copyrightTagsChipGroup
            }
            4 -> {
                textColor = R.color.tag_character
                group = tagDetailsBinding.characterTagsChipGroup
            }
            5 -> {
                textColor = R.color.tag_metadata
                group = tagDetailsBinding.metadataTagsChipGroup
            }
        }

        this.apply {
            text = if(tag.excluded) {
                "-${tag.name}"
            } else {
                tag.name
            }
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
                viewModel.toggleTag(tag.name)
            }

            group.addView(this)
        }
    }
}