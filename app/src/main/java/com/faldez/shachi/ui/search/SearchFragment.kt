package com.faldez.shachi.ui.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.SearchFragmentBinding
import com.faldez.shachi.databinding.TagsDetailsBinding
import com.faldez.shachi.model.Modifier
import com.faldez.shachi.model.ServerView
import com.faldez.shachi.model.TagDetail
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.BooruService
import com.faldez.shachi.util.StringUtil
import com.faldez.shachi.util.clearAllGroup
import com.faldez.shachi.util.getGroupHeaderTextColor
import com.faldez.shachi.util.hideAll
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SearchFragment : DialogFragment() {
    private lateinit var binding: SearchFragmentBinding
    private lateinit var tagDetailsBinding: TagsDetailsBinding

    private val viewModel: SearchSimpleViewModel by viewModels {
        val server: ServerView? =
            requireArguments().getParcelable<ServerView>("server") as ServerView?
        SearchViewModelFactory(server, TagRepository(BooruService(),
            AppDatabase.build(requireContext())), this)
    }

    private lateinit var searchSuggestionAdapter: SearchSuggestionAdapter

    private var isTablet = false

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        isTablet = resources.getBoolean(R.bool.isTablet)
        return MaterialAlertDialogBuilder(requireContext()).create()
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppBar()

        if (isTablet)
            (dialog as AlertDialog?)?.setView(view)
    }

    private fun bindSelectedTags(initialTags: String) {
        binding.loadingIndicator.isVisible = initialTags.isNotEmpty()

        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                val tags = state.selectedTags
                if (tags is SelectedTags.Simple) {
                    binding.selectedTagsHeader.isVisible = tags.isNotEmpty()
                    tagDetailsBinding.hideAll()
                    tagDetailsBinding.clearAllGroup()

                    tags.tags.groupBy { it.type }.forEach { (type, tags) ->
                        val (group, header, textColor) = tagDetailsBinding.getGroupHeaderTextColor(
                            type)

                        header.isVisible =
                            tags.any { it.modifier != Modifier.Minus }
                        group.isVisible = tags.isNotEmpty()

                        tags.forEach { tag ->
                            val chip = Chip(requireContext())
                            chip.bind(group, textColor, tag)
                        }
                    }
                    binding.loadingIndicator.isVisible = false
                }
            }
        }
        viewModel.setInitialTags(initialTags)
        if (viewModel.state.value.isAdvancedMode) {
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
        binding.searchSimpleAppBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())

        binding.searchSimpleTopAppBar.menu.clear()
        binding.searchSimpleTopAppBar.inflateMenu(R.menu.search_menu)
        binding.searchSimpleTopAppBar.setNavigationIcon(if (isTablet) {
            R.drawable.ic_baseline_close_24
        } else {
            R.drawable.ic_baseline_arrow_back_24
        })
        binding.searchSimpleTopAppBar.setNavigationOnClickListener {
            if (isTablet) {
                dialog?.dismiss()
            } else {
                requireActivity().onBackPressed()
            }
        }
        binding.searchSimpleTopAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_button -> {
                    binding.searchSimpleTagsInputText.text?.clear()
                    true
                }
                R.id.apply_button -> {
                    applySearch()
                    true
                }
                R.id.search_mode_button -> {
                    viewModel.toggleMode()
                    true
                }
                else -> false
            }
        }
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.searchSimpleTopAppBar.menu.findItem(R.id.search_mode_button).isChecked =
                    state.isAdvancedMode
            }
        }
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
        val bundle = bundleOf("server" to viewModel.state.value.server,
            "tags" to value)
        findNavController().navigate(R.id.action_search_to_browse, bundle)
        dialog?.dismiss()
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
                        viewModel.insertTagByName(it)
                    }
                }
            }
            doOnTextChanged { text, start, _, _ ->
                if (viewModel.state.value.isAdvancedMode) {
                    text?.toString()?.trim()?.let {
                        if (it.isNotEmpty()) {
                            val tag = StringUtil.getCurrentToken(it, start)
                            viewModel.accept(UiAction.SearchTag(tag))
                        }
                    }
                } else {
                    val isVisible = if (text.isNullOrEmpty()) {
                        searchSuggestionAdapter.clear()
                        binding.selectedTagsLayout.show()
                        binding.suggestionTagLayout.hide()
                        false
                    } else {
                        viewModel.accept(UiAction.SearchTag(text.toString()))
                        binding.selectedTagsLayout.hide()
                        binding.suggestionTagLayout.show()
                        true
                    }
                    binding.loadingIndicator.isVisible = isVisible
                    binding.searchSimpleTopAppBar.menu.findItem(R.id.clear_button).isVisible =
                        isVisible
                }
            }
            setOnEditorActionListener { textView, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    /*
                    If advanced search is active, action done will apply search
                    instead of insert tag into selected tags or apply search if empty
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
                binding.suggestionTagsHeader.isVisible = !it.isNullOrEmpty()
                binding.loadingIndicator.isVisible = false
                it?.let { tags ->
                    searchSuggestionAdapter.setSuggestion(tags)
                }
            }
        }
    }

    private fun Chip.bind(group: ChipGroup, textColor: Int?, tag: TagDetail) {
        text = tag.toString()
        isCloseIconVisible = true
        textColor?.let {
            setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                textColor,
                requireActivity().theme)))
        }
        setOnCloseIconClickListener {
            Log.d("SearchSimpleViewModel", "onClose")
            viewModel.removeTag(tag)
        }
        setOnClickListener {
            viewModel.toggleTag(tag.name)
        }

        if (tag.modifier == Modifier.Minus) {
            tagDetailsBinding.blacklistTagsHeader.isVisible = true
            tagDetailsBinding.blacklistTagsChipGroup.isVisible = true
            tagDetailsBinding.blacklistTagsChipGroup.addView(this)
        } else {
            group.addView(this)
        }
    }
}