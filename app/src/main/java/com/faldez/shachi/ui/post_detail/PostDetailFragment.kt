package com.faldez.shachi.ui.post_detail

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.faldez.shachi.R
import com.faldez.shachi.database.AppDatabase
import com.faldez.shachi.databinding.PostDetailFragmentBinding
import com.faldez.shachi.databinding.TagsDetailsBinding
import com.faldez.shachi.model.Modifier
import com.faldez.shachi.model.Post
import com.faldez.shachi.repository.ServerRepository
import com.faldez.shachi.repository.TagRepository
import com.faldez.shachi.service.BooruService
import com.faldez.shachi.util.clearAllGroup
import com.faldez.shachi.util.getGroupHeaderTextColor
import com.faldez.shachi.util.hideAll
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PostDetailFragment : Fragment() {
    private lateinit var binding: PostDetailFragmentBinding
    private lateinit var tagDetailsBinding: TagsDetailsBinding
    private val viewModel: PostDetailViewModel by viewModels {
        val post = requireArguments().get("post") as Post
        val currentSearchTags = requireArguments().getString("tags", null) ?: ""
        val db = AppDatabase.build(requireContext())
        PostDetailViewModelFactory(post, currentSearchTags, ServerRepository(db),
            TagRepository(BooruService(), db),
            this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = PostDetailFragmentBinding.inflate(inflater, container, false)
        tagDetailsBinding = TagsDetailsBinding.bind(binding.root)
        binding.bind()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareAppbar()
    }

    private fun prepareAppbar() {
        binding.postDetailTopappbar.menu.clear()
        binding.postDetailTopappbar.inflateMenu(R.menu.post_detail_menu)
        binding.postDetailTopappbar.setNavigationIcon(R.drawable.ic_baseline_close_24)
        binding.postDetailTopappbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.postDetailTopappbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search_button -> {
                    applySearch()
                    true
                }
                else -> false
            }
        }
        lifecycleScope.launch {
            viewModel.appendedTagsState.collectLatest {
                binding.postDetailTopappbar.menu.findItem(R.id.search_button).isVisible =
                    it.isNotEmpty()
            }
        }
    }

    private fun applySearch() {
        val tags =
            viewModel.state.value.initialSearchTags + " " + viewModel.appendedTagsState.value.joinToString(
                " ") { it.tag.toString() }
        Log.d("PostDetailFragment", "applySearch $tags")
        val bundle = bundleOf("server" to viewModel.state.value.server,
            "tags" to tags)
        findNavController().navigate(R.id.action_postdetail_to_browse, bundle)
    }

    private fun PostDetailFragmentBinding.bind() {
        viewModel.post.let { p ->
            sizeTextview.text = "${p.width}x${p.height}"
            sourceUrl.text = p.source
            ratingTextview.text = p.rating.toString()
            scoreTextview.text = "${p.score ?: 0}"
            postedTextview.text = p.createdAt
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.state.collectLatest { state ->
                    Log.d("PostDetailFragment", "state $state")

                    loadingLabel.isVisible = state.tags == null
                    tagDetailsBinding.hideAll()
                    tagDetailsBinding.clearAllGroup()

                    state.tags?.groupBy { it.tag.type }?.forEach { (type, tags) ->
                        val (group, header, textColor) = tagDetailsBinding.getGroupHeaderTextColor(
                            type)
                        header.isVisible = tags.any { it.tag.modifier != Modifier.Minus }
                        group.isVisible = tags.isNotEmpty()

                        tags.forEach { tag ->
                            val chip = Chip(requireContext())
                            chip.bind(group, textColor, tag)
                        }
                    }
                }
            }
        }
    }

    private fun Chip.bind(group: ChipGroup, textColor: Int?, tag: TagDetailState) {
        Log.d("PostDetailFragment", "$tag")

        setChipDrawable(ChipDrawable.createFromResource(requireContext(), R.xml.filter_chip))
        text = tag.tag.toString()
        textColor?.let {
            setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources,
                textColor,
                requireActivity().theme)))
        }
        isChecked = tag.checked
        setEnsureMinTouchTargetSize(false)
        isCloseIconVisible = !tag.checked && tag.tag.modifier != Modifier.Minus
        setOnCloseIconClickListener {
            Log.d("PostDetailFragment", "${tag.tag.name} closed")
            viewModel.addTag(tag.copy(checked = true,
                tag = tag.tag.copy(modifier = Modifier.Minus)))
        }
        setOnCheckedChangeListener { chip, isChecked ->
            isCloseIconVisible = !isChecked && tag.tag.modifier != Modifier.Minus
            if (isChecked && tag.mutable) {
                viewModel.addTag(tag.copy(checked = isChecked))
            } else {
                viewModel.removeTag(tag)
            }
        }
        isCheckable = tag.mutable
        if (tag.tag.modifier == Modifier.Minus) {
            tagDetailsBinding.blacklistTagsHeader.isVisible = true
            tagDetailsBinding.blacklistTagsChipGroup.isVisible = true
            tagDetailsBinding.blacklistTagsChipGroup.addView(this)
        } else {
            group.addView(this)
        }
    }
}